# =====================================================================
# LIMS acceptance checks (PowerShell 5.1)
# Scope: auth, dictionaries, catalog, readers, loans, fines,
#        notifications, PDF reports, statistics, audit, admin section,
#        access rights, error handling.
# All messages are ASCII to keep the script encoding-safe.
# Test data is created with a unique suffix and removed on completion.
# Run: powershell -ExecutionPolicy Bypass -File .\acceptance_checks.ps1
# =====================================================================

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$results = New-Object System.Collections.Generic.List[string]
$script:passCount = 0
$script:failCount = 0
$script:skipCount = 0

function Add-Result([string]$status, [string]$name, [string]$details) {
    $line = '{0,-4} | {1,-56} | {2}' -f $status, $name, $details
    $script:results.Add($line) | Out-Null
    Write-Host $line
}

function Check([string]$name, $condition, [string]$details = '') {
    if ($condition) {
        $script:passCount++
        Add-Result 'PASS' $name $details
    } else {
        $script:failCount++
        Add-Result 'FAIL' $name $details
    }
}

function Skip([string]$name, [string]$reason) {
    $script:skipCount++
    Add-Result 'SKIP' $name $reason
}

function Call([string]$method, [string]$path, [string]$token, $bodyObj = $null) {
    $headers = @{}
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    $params = @{
        Uri = "$base$path"
        Method = $method
        Headers = $headers
        UseBasicParsing = $true
        TimeoutSec = 30
    }
    if ($null -ne $bodyObj) {
        $params['Body'] = ($bodyObj | ConvertTo-Json -Compress -Depth 6)
        $params['ContentType'] = 'application/json'
    }
    try {
        $response = Invoke-WebRequest @params
        $parsed = $null
        if ($response.Content) {
            try { $parsed = $response.Content | ConvertFrom-Json } catch { }
        }
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Body = $parsed; Raw = [string]$response.Content }
    } catch {
        $webResponse = $_.Exception.Response
        if ($webResponse) {
            try {
                $reader = New-Object System.IO.StreamReader($webResponse.GetResponseStream())
                $text = $reader.ReadToEnd()
            } catch {
                $text = ''
            }
            return [pscustomobject]@{ Status = [int]$webResponse.StatusCode; Body = $null; Raw = $text }
        }
        return [pscustomobject]@{ Status = 0; Body = $null; Raw = $_.Exception.Message }
    }
}

function CallRaw([string]$method, [string]$path, [string]$token, [string]$rawBody) {
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    try {
        $response = Invoke-WebRequest -Uri "$base$path" -Method $method -Headers $headers -Body $rawBody -UseBasicParsing -TimeoutSec 30
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Raw = [string]$response.Content }
    } catch {
        $webResponse = $_.Exception.Response
        if ($webResponse) {
            try {
                $reader = New-Object System.IO.StreamReader($webResponse.GetResponseStream())
                $text = $reader.ReadToEnd()
            } catch { $text = '' }
            return [pscustomobject]@{ Status = [int]$webResponse.StatusCode; Raw = $text }
        }
        return [pscustomobject]@{ Status = 0; Raw = $_.Exception.Message }
    }
}

function CheckStatus([string]$name, $response, [int]$expected) {
    Check $name ($response.Status -eq $expected) "HTTP $($response.Status), expected $expected"
}

function CheckReport([string]$name, [string]$path, [string]$token) {
    $file = Join-Path $env:TEMP ("lims_report_" + [guid]::NewGuid().ToString('N') + ".pdf")
    try {
        Invoke-WebRequest -Uri "$base$path" -Headers @{ Authorization = "Bearer $token" } -UseBasicParsing -OutFile $file -TimeoutSec 60 | Out-Null
        $bytes = [System.IO.File]::ReadAllBytes($file)
        $signature = [System.Text.Encoding]::ASCII.GetString($bytes[0..3])
        Check $name ($bytes.Length -gt 1000 -and $signature -eq '%PDF') "size=$($bytes.Length) sig=$signature"
    } catch {
        Check $name $false $_.Exception.Message
    } finally {
        Remove-Item $file -ErrorAction SilentlyContinue
    }
}

function Login([string]$email, [string]$password) {
    return Call 'POST' '/api/auth/login' '' @{ email = $email; password = $password }
}

# ---------------------------------------------------------------------
# Readiness
# ---------------------------------------------------------------------
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $null = Invoke-WebRequest -Uri "$base/api/dictionaries/genres" -UseBasicParsing -TimeoutSec 2
        $ready = $true
        break
    } catch {
        Start-Sleep -Milliseconds 1000
    }
}
if (-not $ready) {
    Write-Host 'FATAL: backend is not available on http://localhost:8080'
    exit 1
}

# Test data holders for final cleanup
$tempReaderId = $null
$tempStaffId = $null
$tempBookId = $null
$tempBook2Id = $null
$tempCopyId = $null
$tempGenreId = $null
$tempPublisherId = $null
$tempLocationId = $null
$tempLanguageId = $null
$loanId = $null

try {
    # -----------------------------------------------------------------
    # A. Authentication and access control
    # -----------------------------------------------------------------
    $adminLogin = Login 'admin@lims.com' 'admin123'
    Check 'A01 admin login' ($adminLogin.Status -eq 200 -and $adminLogin.Body.token) "HTTP $($adminLogin.Status)"
    $admin = $adminLogin.Body.token

    $libLogin = Login 'librarian@lims.com' 'librarian123'
    Check 'A02 librarian login' ($libLogin.Status -eq 200 -and $libLogin.Body.token) "HTTP $($libLogin.Status)"
    $lib = $libLogin.Body.token

    $badLogin = Login 'admin@lims.com' 'wrong-password'
    CheckStatus 'A03 login with wrong password' $badLogin 401

    $noToken = Call 'GET' '/api/users?page=0&size=1' ''
    CheckStatus 'A04 protected endpoint without token' $noToken 401

    $garbage = Call 'GET' '/api/users?page=0&size=1' 'garbage.jwt.token'
    CheckStatus 'A05 protected endpoint with invalid JWT' $garbage 401

    $publicBooks = Call 'GET' '/api/books?page=0&size=1' ''
    CheckStatus 'A06 public catalog access without token' $publicBooks 200

    $publicDict = Call 'GET' '/api/dictionaries/genres' ''
    CheckStatus 'A07 public dictionary access without token' $publicDict 200

    $apiDocs = Call 'GET' '/v3/api-docs' ''
    CheckStatus 'A08 OpenAPI docs available' $apiDocs 200

    $libAudit = Call 'GET' '/api/audit?page=0&size=5' $lib
    CheckStatus 'A09 librarian cannot read audit log' $libAudit 403

    $libParams = Call 'GET' '/api/admin/system-parameters' $lib
    CheckStatus 'A10 librarian cannot read admin parameters' $libParams 403

    # -----------------------------------------------------------------
    # B. Dictionaries (read)
    # -----------------------------------------------------------------
    $genres = Call 'GET' '/api/dictionaries/genres' $admin
    Check 'B01 genres dictionary' ($genres.Status -eq 200 -and @($genres.Body).Count -ge 3) "HTTP $($genres.Status), count=$(@($genres.Body).Count)"

    $languages = Call 'GET' '/api/dictionaries/languages' $admin
    Check 'B02 languages dictionary' ($languages.Status -eq 200 -and @($languages.Body).Count -ge 1) "HTTP $($languages.Status), count=$(@($languages.Body).Count)"

    $publishers = Call 'GET' '/api/dictionaries/publishers' $admin
    Check 'B03 publishers dictionary' ($publishers.Status -eq 200 -and @($publishers.Body).Count -ge 1) "HTTP $($publishers.Status), count=$(@($publishers.Body).Count)"

    $locations = Call 'GET' '/api/dictionaries/locations' $admin
    Check 'B04 locations dictionary' ($locations.Status -eq 200 -and @($locations.Body).Count -ge 1) "HTTP $($locations.Status), count=$(@($locations.Body).Count)"

    $roles = Call 'GET' '/api/dictionaries/roles' $admin
    Check 'B05 roles dictionary' ($roles.Status -eq 200 -and @($roles.Body).Count -eq 3) "HTTP $($roles.Status), count=$(@($roles.Body).Count)"

    $userStatuses = Call 'GET' '/api/dictionaries/user-statuses' $admin
    Check 'B06 user statuses dictionary' ($userStatuses.Status -eq 200 -and @($userStatuses.Body).Count -ge 4) "HTTP $($userStatuses.Status), count=$(@($userStatuses.Body).Count)"

    $itemStatuses = Call 'GET' '/api/dictionaries/item-statuses' $admin
    Check 'B07 item statuses dictionary' ($itemStatuses.Status -eq 200 -and @($itemStatuses.Body).Count -ge 5) "HTTP $($itemStatuses.Status), count=$(@($itemStatuses.Body).Count)"

    $loanStatuses = Call 'GET' '/api/dictionaries/loan-statuses' $admin
    Check 'B08 loan statuses dictionary' ($loanStatuses.Status -eq 200 -and @($loanStatuses.Body).Count -ge 4) "HTTP $($loanStatuses.Status), count=$(@($loanStatuses.Body).Count)"

    # -----------------------------------------------------------------
    # C. Catalog: books, copies, authors, magazines
    # -----------------------------------------------------------------
    $books = Call 'GET' '/api/books?page=0&size=50' $admin
    Check 'C01 books list' ($books.Status -eq 200 -and @($books.Body.content).Count -ge 5) "HTTP $($books.Status), content=$(@($books.Body.content).Count), total=$($books.Body.totalElements)"

    $demoBook = @($books.Body.content) | Select-Object -First 1
    $bookById = Call 'GET' "/api/books/$($demoBook.id)" $admin
    Check 'C02 book card by id' ($bookById.Status -eq 200 -and $bookById.Body.id -eq $demoBook.id) "HTTP $($bookById.Status)"

    $search = Call 'GET' '/api/books/search?title=1984&page=0&size=10' $admin
    Check 'C03 book search by title' ($search.Status -eq 200 -and @($search.Body.content).Count -ge 1) "HTTP $($search.Status), count=$(@($search.Body.content).Count)"

    $authors = Call 'GET' '/api/authors' $admin
    Check 'C04 authors list' ($authors.Status -eq 200 -and @($authors.Body).Count -ge 1) "HTTP $($authors.Status), count=$(@($authors.Body).Count)"

    $magazines = Call 'GET' '/api/magazines?page=0&size=10' $admin
    Check 'C05 magazines list' ($magazines.Status -eq 200) "HTTP $($magazines.Status), content=$(@($magazines.Body.content).Count)"

    $genreId = @($genres.Body)[0].id
    $languageId = @($languages.Body)[0].id

    $tempBook = @{
        title = "Acceptance Test Book $stamp"
        publicationDate = '2026-01-15'
        isbn = "ACC-$stamp"
        languageId = $languageId
        genreId = $genreId
        pageCount = 120
        authorIds = @()
    }
    $createdBook = Call 'POST' '/api/books' $admin $tempBook
    Check 'C06 create book' ($createdBook.Status -eq 200 -and $createdBook.Body.id) "HTTP $($createdBook.Status)"
    $tempBookId = $createdBook.Body.id

    $updatedBookBody = @{
        title = "Acceptance Test Book $stamp (updated)"
        publicationDate = '2026-01-15'
        isbn = "ACC-$stamp"
        languageId = $languageId
        genreId = $genreId
        pageCount = 121
        authorIds = @()
    }
    $updatedBook = Call 'PUT' "/api/books/$tempBookId" $admin $updatedBookBody
    Check 'C07 update book' ($updatedBook.Status -eq 200 -and $updatedBook.Body.title -like '*updated*') "HTTP $($updatedBook.Status)"

    $bookAfterUpdate = Call 'GET' "/api/books/$tempBookId" $admin
    Check 'C08 read updated book' ($bookAfterUpdate.Status -eq 200 -and $bookAfterUpdate.Body.pageCount -eq 121) "HTTP $($bookAfterUpdate.Status), pages=$($bookAfterUpdate.Body.pageCount)"

    $tempCopyBody = @{
        libraryItemId = $tempBookId
        inventoryNumber = "ACC-INV-$stamp"
        acquisitionDate = '2026-01-20'
        price = 500.00
    }
    $createdCopy = Call 'POST' '/api/item-copies' $admin $tempCopyBody
    Check 'C09 create item copy' ($createdCopy.Status -eq 200 -and $createdCopy.Body.id) "HTTP $($createdCopy.Status)"
    $tempCopyId = $createdCopy.Body.id

    $copyStatus = Call 'PATCH' "/api/item-copies/$tempCopyId/status" $admin @{ status = 'UNDER_REPAIR' }
    Check 'C10 change item copy status' ($copyStatus.Status -eq 200 -and $copyStatus.Body.statusName -eq 'UNDER_REPAIR') "HTTP $($copyStatus.Status), status=$($copyStatus.Body.statusName)"

    $copyBack = Call 'PATCH' "/api/item-copies/$tempCopyId/status" $admin @{ status = 'AVAILABLE' }
    Check 'C11 restore item copy status' ($copyBack.Status -eq 200 -and $copyBack.Body.statusName -eq 'AVAILABLE') "HTTP $($copyBack.Status), status=$($copyBack.Body.statusName)"

    $copiesByBook = Call 'GET' "/api/item-copies?libraryItemId=$tempBookId" $admin
    Check 'C12 item copies by book' ($copiesByBook.Status -eq 200 -and @($copiesByBook.Body).Count -eq 1) "HTTP $($copiesByBook.Status), count=$(@($copiesByBook.Body).Count)"

    $libBookBody = @{
        title = "Librarian Test Book $stamp"
        publicationDate = '2026-02-01'
        isbn = "LIB-$stamp"
        languageId = $languageId
        genreId = $genreId
        pageCount = 80
        authorIds = @()
    }
    $libBook = Call 'POST' '/api/books' $lib $libBookBody
    Check 'C13 librarian can create book' ($libBook.Status -eq 200 -and $libBook.Body.id) "HTTP $($libBook.Status)"
    $tempBook2Id = $libBook.Body.id

    $libDeleteBook = Call 'DELETE' "/api/books/$tempBook2Id" $lib
    CheckStatus 'C14 librarian cannot delete book' $libDeleteBook 403

    $adminDeleteBook2 = Call 'DELETE' "/api/books/$tempBook2Id" $admin
    CheckStatus 'C15 admin deletes book' $adminDeleteBook2 200

    $deletedBookRead = Call 'GET' "/api/books/$tempBook2Id" $admin
    CheckStatus 'C16 deleted book returns 404' $deletedBookRead 404

    # -----------------------------------------------------------------
    # D. Readers (users)
    # -----------------------------------------------------------------
    $users = Call 'GET' '/api/users?page=0&size=100' $admin
    $nonDeleted = @($users.Body.content | Where-Object { $_.statusName -ne 'DELETED' })
    Check 'D01 readers list' ($users.Status -eq 200 -and $nonDeleted.Count -ge 5) "HTTP $($users.Status), active=$($nonDeleted.Count), total=$($users.Body.totalElements)"

    $readerEmail = "accept-reader-$stamp@lims.com"
    $newReader = @{
        firstName = 'Acceptance'
        lastName = 'Reader'
        middleName = 'Test'
        email = $readerEmail
        role = 'READER'
    }
    $createdReader = Call 'POST' '/api/users' $lib $newReader
    Check 'D02 librarian registers reader' ($createdReader.Status -eq 200 -and $createdReader.Body.id) "HTTP $($createdReader.Status), role=$($createdReader.Body.roleName)"
    $tempReaderId = $createdReader.Body.id

    $frozen = Call 'PATCH' "/api/users/$tempReaderId/status" $admin @{ status = 'FROZEN' }
    Check 'D03 admin freezes reader' ($frozen.Status -eq 200 -and $frozen.Body.statusName -eq 'FROZEN') "HTTP $($frozen.Status), status=$($frozen.Body.statusName)"

    $availableCopies = Call 'GET' '/api/item-copies?status=AVAILABLE' $admin
    $loanCopy = @($availableCopies.Body | Where-Object { $_.id -ne $tempCopyId }) | Select-Object -First 1
    Check 'D04 available copy exists for issue' ($availableCopies.Status -eq 200 -and $loanCopy) "HTTP $($availableCopies.Status), count=$(@($availableCopies.Body).Count)"

    $issueFrozen = Call 'POST' "/api/loans/issue/by-copy?userId=$tempReaderId&copyId=$($loanCopy.id)" $admin
    CheckStatus 'D05 issue denied for frozen reader' $issueFrozen 400

    $activated = Call 'PATCH' "/api/users/$tempReaderId/status" $admin @{ status = 'ACTIVE' }
    Check 'D06 admin activates reader' ($activated.Status -eq 200 -and $activated.Body.statusName -eq 'ACTIVE') "HTTP $($activated.Status), status=$($activated.Body.statusName)"

    $limit = Call 'PATCH' "/api/users/$tempReaderId/max-loans" $admin @{ maxActiveLoans = 3 }
    Check 'D07 change reader loan limit' ($limit.Status -eq 200 -and $limit.Body.maxActiveLoans -eq 3) "HTTP $($limit.Status), limit=$($limit.Body.maxActiveLoans)"

    $password = Call 'PATCH' "/api/users/$tempReaderId/password" $admin @{ password = 'reader12345' }
    CheckStatus 'D08 set reader password' $password 204

    $readerLogin = Login $readerEmail 'reader12345'
    Check 'D09 reader login with new password' ($readerLogin.Status -eq 200 -and $readerLogin.Body.token) "HTTP $($readerLogin.Status)"

    $roleUp = Call 'PATCH' "/api/users/$tempReaderId/role" $admin @{ role = 'LIBRARIAN' }
    Check 'D10 admin changes reader role' ($roleUp.Status -eq 200 -and $roleUp.Body.roleName -eq 'LIBRARIAN') "HTTP $($roleUp.Status), role=$($roleUp.Body.roleName)"

    $roleDown = Call 'PATCH' "/api/users/$tempReaderId/role" $admin @{ role = 'READER' }
    Check 'D11 admin restores reader role' ($roleDown.Status -eq 200 -and $roleDown.Body.roleName -eq 'READER') "HTTP $($roleDown.Status), role=$($roleDown.Body.roleName)"

    $searchReader = Call 'GET' "/api/users/search?q=$readerEmail" $admin
    Check 'D12 reader search by email' ($searchReader.Status -eq 200 -and @($searchReader.Body).Count -eq 1) "HTTP $($searchReader.Status), count=$(@($searchReader.Body).Count)"

    # Admin creates a staff account
    $staffEmail = "accept-staff-$stamp@lims.com"
    $newStaff = @{
        firstName = 'Acceptance'
        lastName = 'Staff'
        middleName = 'Test'
        email = $staffEmail
        role = 'LIBRARIAN'
        password = 'staff12345'
    }
    $createdStaff = Call 'POST' '/api/users' $admin $newStaff
    Check 'D13 admin creates staff account' ($createdStaff.Status -eq 200 -and $createdStaff.Body.id) "HTTP $($createdStaff.Status), role=$($createdStaff.Body.roleName)"
    $tempStaffId = $createdStaff.Body.id

    $staffLogin = Login $staffEmail 'staff12345'
    Check 'D14 staff login' ($staffLogin.Status -eq 200 -and $staffLogin.Body.token) "HTTP $($staffLogin.Status)"

    $staffPassword = Call 'PATCH' "/api/users/$tempStaffId/password" $admin @{ password = 'staff54321' }
    CheckStatus 'D15 change staff password' $staffPassword 204

    $staffLogin2 = Login $staffEmail 'staff54321'
    Check 'D16 staff login with new password' ($staffLogin2.Status -eq 200 -and $staffLogin2.Body.token) "HTTP $($staffLogin2.Status)"

    $libCreateStaff = Call 'POST' '/api/users' $lib @{
        firstName = 'Acceptance'
        lastName = 'Forbidden'
        middleName = 'Test'
        email = "accept-forbidden-$stamp@lims.com"
        role = 'LIBRARIAN'
        password = 'forbidden123'
    }
    CheckStatus 'D17 librarian cannot create staff' $libCreateStaff 403

    $libChangeRole = Call 'PATCH' "/api/users/$tempReaderId/role" $lib @{ role = 'ADMIN' }
    CheckStatus 'D18 librarian cannot change roles' $libChangeRole 403

    $libDeleteUser = Call 'DELETE' "/api/users/$tempReaderId" $lib
    CheckStatus 'D19 librarian cannot delete reader' $libDeleteUser 403

    # -----------------------------------------------------------------
    # E. Loans: issue, renew, return
    # -----------------------------------------------------------------
    $issue = Call 'POST' "/api/loans/issue/by-copy?userId=$tempReaderId&copyId=$($loanCopy.id)" $lib
    Check 'E01 librarian issues copy' ($issue.Status -eq 200 -and $issue.Body.id -and $issue.Body.statusName -eq 'ACTIVE') "HTTP $($issue.Status), status=$($issue.Body.statusName)"
    $loanId = $issue.Body.id
    $initialDue = $issue.Body.dueDate

    $activeLoans = Call 'GET' '/api/loans/active' $lib
    $activeIds = @($activeLoans.Body | ForEach-Object { $_.id })
    Check 'E02 active loans contain issued loan' ($activeLoans.Status -eq 200 -and $activeIds -contains $loanId) "HTTP $($activeLoans.Status), count=$($activeIds.Count)"

    $overdueLoan = @($activeLoans.Body | Where-Object { $_.statusName -eq 'OVERDUE' }) | Select-Object -First 1
    if ($overdueLoan) {
        Check 'E03 overdue active loan has fine amount' ([decimal]$overdueLoan.fineAmount -gt 0) "fine=$($overdueLoan.fineAmount)"
    } else {
        Skip 'E03 overdue active loan has fine amount' 'no overdue demo loan'
    }

    $history = Call 'GET' "/api/loans/history/user/$tempReaderId" $lib
    Check 'E04 reader loan history' ($history.Status -eq 200 -and @($history.Body | Where-Object { $_.id -eq $loanId }).Count -eq 1) "HTTP $($history.Status), count=$(@($history.Body).Count)"

    $deleteActive = Call 'DELETE' "/api/users/$tempReaderId" $admin
    CheckStatus 'E05 cannot delete reader with active loan' $deleteActive 400

    $renew = Call 'POST' "/api/loans/$loanId/renew?extraDays=14" $lib
    Check 'E06 renew loan for 14 days' ($renew.Status -eq 200 -and $renew.Body.statusName -eq 'RENEWED' -and $renew.Body.dueDate -gt $initialDue) "HTTP $($renew.Status), status=$($renew.Body.statusName), due=$($renew.Body.dueDate)"

    $return = Call 'POST' "/api/loans/return?loanId=$loanId" $lib
    Check 'E07 return loan' ($return.Status -eq 200 -and $return.Body.statusName -eq 'RETURNED') "HTTP $($return.Status), status=$($return.Body.statusName)"

    $returnAgain = Call 'POST' "/api/loans/return?loanId=$loanId" $lib
    CheckStatus 'E08 repeated return is rejected' $returnAgain 400

    $activeAfterReturn = Call 'GET' '/api/loans/active' $lib
    $stillActive = @($activeAfterReturn.Body | Where-Object { $_.id -eq $loanId })
    Check 'E09 returned loan left active list' ($activeAfterReturn.Status -eq 200 -and $stillActive.Count -eq 0) "HTTP $($activeAfterReturn.Status)"

    $historyAfter = Call 'GET' "/api/loans/history/user/$tempReaderId" $lib
    $returnedLoan = @($historyAfter.Body | Where-Object { $_.id -eq $loanId }) | Select-Object -First 1
    Check 'E10 returned loan kept in history' ($historyAfter.Status -eq 200 -and $returnedLoan -and $returnedLoan.statusName -eq 'RETURNED') "HTTP $($historyAfter.Status), status=$($returnedLoan.statusName)"

    $busyBook = $null
    foreach ($candidate in @($books.Body.content)) {
        $candidateCopies = Call 'GET' "/api/item-copies?libraryItemId=$($candidate.id)" $admin
        $candidateAvailable = @($candidateCopies.Body | Where-Object { $_.statusName -eq 'AVAILABLE' })
        if (@($candidateCopies.Body).Count -gt 0 -and $candidateAvailable.Count -eq 0) {
            $busyBook = $candidate
            break
        }
    }
    if ($busyBook) {
        $issueNoCopies = Call 'POST' "/api/loans/issue/by-item?userId=$tempReaderId&libraryItemId=$($busyBook.id)" $admin
        CheckStatus 'E11 issue fails when no copies available' $issueNoCopies 400
    } else {
        Skip 'E11 issue fails when no copies available' 'no busy demo book'
    }

    $overdue = Call 'GET' '/api/loans/overdue' $lib
    Check 'E12 overdue loans list' ($overdue.Status -eq 200) "HTTP $($overdue.Status), count=$(@($overdue.Body).Count)"

    $summary = Call 'GET' '/api/loans/statistics/summary' $lib
    Check 'E13 summary statistics' ($summary.Status -eq 200 -and $null -ne $summary.Body.totalLoans -and $null -ne $summary.Body.activeLoans -and $null -ne $summary.Body.overdueReturns) "HTTP $($summary.Status), total=$($summary.Body.totalLoans), active=$($summary.Body.activeLoans)"

    $popular = Call 'GET' '/api/loans/statistics/popular-books?limit=5' $lib
    Check 'E14 popular books statistics' ($popular.Status -eq 200 -and @($popular.Body).Count -ge 1) "HTTP $($popular.Status), count=$(@($popular.Body).Count)"

    # -----------------------------------------------------------------
    # F. Fines
    # -----------------------------------------------------------------
    $fines = Call 'GET' '/api/fines' $lib
    Check 'F01 fines list' ($fines.Status -eq 200 -and @($fines.Body).Count -ge 3) "HTTP $($fines.Status), count=$(@($fines.Body).Count)"

    $paidFines = Call 'GET' '/api/fines?paid=true' $lib
    $paidFine = @($paidFines.Body) | Select-Object -First 1
    Check 'F02 paid fines filter' ($paidFines.Status -eq 200 -and @($paidFines.Body).Count -ge 1 -and ($paidFines.Body | Where-Object { -not $_.isPaid }).Count -eq 0) "HTTP $($paidFines.Status), count=$(@($paidFines.Body).Count)"

    if ($paidFine) {
        $payAgain = Call 'PATCH' "/api/fines/$($paidFine.id)/pay" $lib
        CheckStatus 'F03 repeated payment is rejected' $payAgain 400
    } else {
        Skip 'F03 repeated payment is rejected' 'no paid fine in demo data'
    }

    $unpaidFines = Call 'GET' '/api/fines?paid=false' $lib
    Check 'F04 unpaid fines filter' ($unpaidFines.Status -eq 200) "HTTP $($unpaidFines.Status), count=$(@($unpaidFines.Body).Count)"

    # -----------------------------------------------------------------
    # G. Notifications
    # -----------------------------------------------------------------
    $notifications = Call 'GET' '/api/notifications' $lib
    Check 'G01 notifications list' ($notifications.Status -eq 200 -and @($notifications.Body).Count -ge 1) "HTTP $($notifications.Status), count=$(@($notifications.Body).Count)"

    $notificationTypes = @($notifications.Body | ForEach-Object { $_.typeName } | Where-Object { $_ } | Select-Object -Unique)
    Check 'G02 notifications have type' ($notifications.Status -eq 200 -and $notificationTypes.Count -ge 1) "types=$($notificationTypes -join ',')"

    # -----------------------------------------------------------------
    # H. PDF reports
    # -----------------------------------------------------------------
    CheckReport 'H01 popular books PDF report' '/api/reports/popular-books' $lib
    CheckReport 'H02 active readers PDF report' '/api/reports/active-readers' $lib
    CheckReport 'H03 fine debts PDF report' '/api/reports/fine-debts' $lib
    CheckReport 'H04 overdue loans PDF report' '/api/reports/overdue-loans' $lib

    # -----------------------------------------------------------------
    # J. Audit (admin)
    # -----------------------------------------------------------------
    $audit = Call 'GET' '/api/audit?page=0&size=100' $admin
    Check 'J01 admin reads audit log' ($audit.Status -eq 200 -and $audit.Body.totalElements -gt 0) "HTTP $($audit.Status), total=$($audit.Body.totalElements)"

    $bookCreateAudit = @($audit.Body.content | Where-Object { $_.actionName -eq 'CREATE' -and $_.entityType -eq 'BOOK' })
    Check 'J02 audit contains book creation' ($bookCreateAudit.Count -ge 1) "entries=$($bookCreateAudit.Count)"

    $userAudit = @($audit.Body.content | Where-Object { $_.entityType -eq 'USER' })
    Check 'J03 audit contains user events' ($userAudit.Count -ge 1) "entries=$($userAudit.Count)"

    $loanAudit = @($audit.Body.content | Where-Object { $_.entityType -eq 'LOAN' })
    Check 'J04 audit contains loan events' ($loanAudit.Count -ge 1) "entries=$($loanAudit.Count)"

    # -----------------------------------------------------------------
    # K. Admin: system parameters
    # -----------------------------------------------------------------
    $params = Call 'GET' '/api/admin/system-parameters' $admin
    Check 'K01 admin reads system parameters' ($params.Status -eq 200 -and @($params.Body).Count -eq 3) "HTTP $($params.Status), count=$(@($params.Body).Count)"

    $paramKeys = @($params.Body | ForEach-Object { $_.key })
    Check 'K02 required parameters present' ($paramKeys -contains 'fine_per_day' -and $paramKeys -contains 'loan_period_days' -and $paramKeys -contains 'max_active_loans_default') "keys=$($paramKeys -join ',')"

    $sameParam = Call 'PUT' '/api/admin/system-parameters/max_active_loans_default' $admin @{ value = '5' }
    Check 'K03 save system parameter' ($sameParam.Status -eq 200 -and $sameParam.Body.value -eq '5') "HTTP $($sameParam.Status), value=$($sameParam.Body.value)"

    $changedParam = Call 'PUT' '/api/admin/system-parameters/fine_per_day' $admin @{ value = '11.00' }
    Check 'K04 change fine rate' ($changedParam.Status -eq 200 -and $changedParam.Body.value -eq '11.00') "HTTP $($changedParam.Status), value=$($changedParam.Body.value)"

    $restoredParam = Call 'PUT' '/api/admin/system-parameters/fine_per_day' $admin @{ value = '10.00' }
    Check 'K05 restore fine rate' ($restoredParam.Status -eq 200 -and $restoredParam.Body.value -eq '10.00') "HTTP $($restoredParam.Status), value=$($restoredParam.Body.value)"

    $invalidParam = Call 'PUT' '/api/admin/system-parameters/fine_per_day' $admin @{ value = 'abc' }
    CheckStatus 'K06 invalid parameter value rejected' $invalidParam 400

    $zeroParam = Call 'PUT' '/api/admin/system-parameters/loan_period_days' $admin @{ value = '0' }
    CheckStatus 'K07 non-positive parameter rejected' $zeroParam 400

    $unknownParam = Call 'PUT' '/api/admin/system-parameters/unknown_key' $admin @{ value = '1' }
    CheckStatus 'K08 unknown parameter returns 404' $unknownParam 404

    # -----------------------------------------------------------------
    # L. Admin: dictionaries CRUD
    # -----------------------------------------------------------------
    $adminGenres = Call 'GET' '/api/admin/dictionaries/genres' $admin
    Check 'L01 admin reads genres' ($adminGenres.Status -eq 200 -and @($adminGenres.Body).Count -ge 3) "HTTP $($adminGenres.Status), count=$(@($adminGenres.Body).Count)"

    $newGenre = Call 'POST' '/api/admin/dictionaries/genres' $admin @{ name = "Acceptance genre $stamp" }
    Check 'L02 create genre' ($newGenre.Status -eq 200 -and $newGenre.Body.id) "HTTP $($newGenre.Status), name=$($newGenre.Body.name)"
    $tempGenreId = $newGenre.Body.id

    $updatedGenre = Call 'PUT' "/api/admin/dictionaries/genres/$tempGenreId" $admin @{ name = "Acceptance genre $stamp updated" }
    Check 'L03 update genre' ($updatedGenre.Status -eq 200 -and $updatedGenre.Body.name -like '*updated*') "HTTP $($updatedGenre.Status)"

    $genreList = Call 'GET' '/api/admin/dictionaries/genres' $admin
    Check 'L04 created genre present in list' ($genreList.Status -eq 200 -and @($genreList.Body | Where-Object { $_.id -eq $tempGenreId }).Count -eq 1) "HTTP $($genreList.Status)"

    $deleteGenre = Call 'DELETE' "/api/admin/dictionaries/genres/$tempGenreId" $admin
    CheckStatus 'L05 delete genre' $deleteGenre 204
    $tempGenreId = $null

    $missingName = Call 'POST' '/api/admin/dictionaries/genres' $admin @{ name = '' }
    CheckStatus 'L06 empty genre name rejected' $missingName 400

    $readonlyDict = Call 'POST' '/api/admin/dictionaries/roles' $admin @{ name = 'Test role' }
    CheckStatus 'L07 readonly dictionary not editable' $readonlyDict 400

    $usedGenreId = (@($books.Body.content | Where-Object { $_.genreId }) | Select-Object -First 1).genreId
    if ($usedGenreId) {
        $deleteUsed = Call 'DELETE' "/api/admin/dictionaries/genres/$usedGenreId" $admin
        CheckStatus 'L08 cannot delete genre in use' $deleteUsed 400
    } else {
        Skip 'L08 cannot delete genre in use' 'no used genre found'
    }

    $newPublisher = Call 'POST' '/api/admin/dictionaries/publishers' $admin @{ name = "Acceptance publisher $stamp"; city = 'Tomsk' }
    Check 'L09 create publisher' ($newPublisher.Status -eq 200 -and $newPublisher.Body.id) "HTTP $($newPublisher.Status), city=$($newPublisher.Body.city)"
    $tempPublisherId = $newPublisher.Body.id

    $updatedPublisher = Call 'PUT' "/api/admin/dictionaries/publishers/$tempPublisherId" $admin @{ name = "Acceptance publisher $stamp"; city = 'Novosibirsk' }
    Check 'L10 update publisher' ($updatedPublisher.Status -eq 200 -and $updatedPublisher.Body.city -eq 'Novosibirsk') "HTTP $($updatedPublisher.Status), city=$($updatedPublisher.Body.city)"

    $deletePublisher = Call 'DELETE' "/api/admin/dictionaries/publishers/$tempPublisherId" $admin
    CheckStatus 'L11 delete publisher' $deletePublisher 204
    $tempPublisherId = $null

    $newLocation = Call 'POST' '/api/admin/dictionaries/locations' $admin @{ hall = 'Hall A'; shelf = 'Shelf 1'; rack = 'Rack 1' }
    Check 'L12 create location' ($newLocation.Status -eq 200 -and $newLocation.Body.id) "HTTP $($newLocation.Status), name=$($newLocation.Body.name)"
    $tempLocationId = $newLocation.Body.id

    $deleteLocation = Call 'DELETE' "/api/admin/dictionaries/locations/$tempLocationId" $admin
    CheckStatus 'L13 delete location' $deleteLocation 204
    $tempLocationId = $null

    $emptyLocation = Call 'POST' '/api/admin/dictionaries/locations' $admin @{ hall = ''; shelf = ''; rack = '' }
    CheckStatus 'L14 empty location rejected' $emptyLocation 400

    $newLanguage = Call 'POST' '/api/admin/dictionaries/languages' $admin @{ name = "Acceptance language $stamp" }
    Check 'L15 create language' ($newLanguage.Status -eq 200 -and $newLanguage.Body.id) "HTTP $($newLanguage.Status), name=$($newLanguage.Body.name)"
    $tempLanguageId = $newLanguage.Body.id

    $deleteLanguage = Call 'DELETE' "/api/admin/dictionaries/languages/$tempLanguageId" $admin
    CheckStatus 'L16 delete language' $deleteLanguage 204
    $tempLanguageId = $null

    $libCreateGenre = Call 'POST' '/api/admin/dictionaries/genres' $lib @{ name = 'Forbidden genre' }
    CheckStatus 'L17 librarian cannot edit dictionaries' $libCreateGenre 403

    $libCanRead = Call 'GET' '/api/books?page=0&size=1' $lib
    CheckStatus 'L18 librarian session intact after 403' $libCanRead 200

    $auditAfterAdmin = Call 'GET' '/api/audit?page=0&size=100' $admin
    $dictAudit = @($auditAfterAdmin.Body.content | Where-Object { $_.entityType -like 'DICTIONARY_*' -or $_.entityType -eq 'SYSTEM_PARAMETER' })
    Check 'L19 audit contains dictionary events' ($auditAfterAdmin.Status -eq 200 -and $dictAudit.Count -ge 1) "entries=$($dictAudit.Count)"

    # -----------------------------------------------------------------
    # M. Error handling
    # -----------------------------------------------------------------
    $badUuid = Call 'GET' '/api/books/not-a-uuid' $admin
    CheckStatus 'M01 malformed UUID rejected' $badUuid 400

    $wrongMethod = Call 'GET' "/api/users/$tempReaderId/status" $admin
    CheckStatus 'M02 unsupported HTTP method' $wrongMethod 405

    $malformed = CallRaw 'POST' '/api/books' $admin '{"title": "Broken", '
    CheckStatus 'M03 malformed JSON rejected' $malformed 400

    $unknown = Call 'GET' '/api/does-not-exist' $admin
    CheckStatus 'M04 unknown endpoint returns 404' $unknown 404

    $unknownBook = Call 'GET' '/api/books/11111111-1111-1111-1111-111111111111' $admin
    CheckStatus 'M05 unknown book returns 404' $unknownBook 404

    # -----------------------------------------------------------------
    # N. Cleanup with verification
    # -----------------------------------------------------------------
    $deleteCopy = Call 'DELETE' "/api/item-copies/$tempCopyId" $admin
    CheckStatus 'N01 delete item copy' $deleteCopy 200
    $tempCopyId = $null

    $deleteBook = Call 'DELETE' "/api/books/$tempBookId" $admin
    CheckStatus 'N02 delete test book' $deleteBook 200
    $tempBookId = $null

    $deleteReader = Call 'DELETE' "/api/users/$tempReaderId" $admin
    CheckStatus 'N03 delete test reader' $deleteReader 204

    $searchDeleted = Call 'GET' "/api/users/search?q=$readerEmail" $admin
    Check 'N04 deleted reader hidden from search' ($searchDeleted.Status -eq 200 -and @($searchDeleted.Body).Count -eq 0) "HTTP $($searchDeleted.Status), count=$(@($searchDeleted.Body).Count)"
    $tempReaderId = $null

    $staffRoleDown = Call 'PATCH' "/api/users/$tempStaffId/role" $admin @{ role = 'READER' }
    CheckStatus 'N05 demote test staff to reader' $staffRoleDown 200

    $deleteStaff = Call 'DELETE' "/api/users/$tempStaffId" $admin
    CheckStatus 'N06 delete test staff' $deleteStaff 204
    $tempStaffId = $null
} finally {
    # Safety cleanup (ignores errors)
    try {
        if ($loanId) { $null = Call 'POST' "/api/loans/return?loanId=$loanId" $admin }
        if ($tempCopyId) { $null = Call 'DELETE' "/api/item-copies/$tempCopyId" $admin }
        if ($tempBookId) { $null = Call 'DELETE' "/api/books/$tempBookId" $admin }
        if ($tempBook2Id) { $null = Call 'DELETE' "/api/books/$tempBook2Id" $admin }
        if ($tempReaderId) { $null = Call 'DELETE' "/api/users/$tempReaderId" $admin }
        if ($tempStaffId) {
            $null = Call 'PATCH' "/api/users/$tempStaffId/role" $admin @{ role = 'READER' }
            $null = Call 'DELETE' "/api/users/$tempStaffId" $admin
        }
        if ($tempGenreId) { $null = Call 'DELETE' "/api/admin/dictionaries/genres/$tempGenreId" $admin }
        if ($tempPublisherId) { $null = Call 'DELETE' "/api/admin/dictionaries/publishers/$tempPublisherId" $admin }
        if ($tempLocationId) { $null = Call 'DELETE' "/api/admin/dictionaries/locations/$tempLocationId" $admin }
        if ($tempLanguageId) { $null = Call 'DELETE' "/api/admin/dictionaries/languages/$tempLanguageId" $admin }
    } catch {
        Write-Host ("Cleanup warning: " + $_.Exception.Message)
    }
}

# ---------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------
$total = $script:passCount + $script:failCount
$summaryLine = 'TOTAL: {0} | PASS: {1} | FAIL: {2} | SKIP: {3}' -f $total, $script:passCount, $script:failCount, $script:skipCount
Write-Host ''
Write-Host $summaryLine

$resultFile = Join-Path 'C:\Users\proec\AppData\Local\Temp\opencode' ("acceptance_results_$stamp.txt")
$header = @(
    'LIMS acceptance checks',
    ("Date: " + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
    ("Backend: " + $base),
    ('')
)
($header + $script:results + @('', $summaryLine)) | Out-File -FilePath $resultFile -Encoding UTF8
Write-Host ("Results saved to: " + $resultFile)

if ($script:failCount -gt 0) { exit 2 }
exit 0
