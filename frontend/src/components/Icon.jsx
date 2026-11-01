const paths = {
  home: (
    <>
      <path d="M2 7.5 8 2l6 5.5" />
      <path d="M4 6.8V14h8V6.8" />
      <path d="M6.5 14v-4h3v4" />
    </>
  ),
  book: (
    <>
      <path d="M3 2.5h6a2 2 0 0 1 2 2V14H5a2 2 0 0 0-2 2z" />
      <path d="M3 2.5v13.5" />
      <path d="M11 4.5h2V14h-2" />
    </>
  ),
  magazine: (
    <>
      <path d="M3 2.5h10v11H3z" />
      <path d="M5 5h6M5 7.5h6M5 10h4" />
    </>
  ),
  authors: (
    <>
      <path d="M3 13.5 4 10l7.5-7.5 2 2L6 12z" />
      <path d="M10 4.5l2 2" />
      <path d="M3 14h10" />
    </>
  ),
  readers: (
    <>
      <circle cx="6" cy="5.5" r="2.5" />
      <path d="M1.5 14c0-2.3 2-4 4.5-4s4.5 1.7 4.5 4" />
      <path d="M10.5 3.7a2.5 2.5 0 0 1 0 3.6" />
      <path d="M12 10.4c1.6.5 2.5 1.8 2.5 3.6" />
    </>
  ),
  loans: (
    <>
      <path d="M2 5h10" />
      <path d="M9 2l3 3-3 3" />
      <path d="M14 11H4" />
      <path d="M7 8l-3 3 3 3" />
    </>
  ),
  statistics: (
    <>
      <path d="M2 14V2" />
      <path d="M2 14h12" />
      <path d="M4.5 11V7M7.5 11V4M10.5 11V6M13 11V3" />
    </>
  ),
  plus: (
    <>
      <path d="M8 3v10M3 8h10" />
    </>
  ),
  search: (
    <>
      <circle cx="7" cy="7" r="4.5" />
      <path d="M10.5 10.5 14 14" />
    </>
  ),
  logout: (
    <>
      <path d="M6 3H3v10h3" />
      <path d="M8 8h6" />
      <path d="M11 5l3 3-3 3" />
    </>
  ),
  refresh: (
    <>
      <path d="M13 6a5.5 5.5 0 1 0 .2 4" />
      <path d="M13 2.5V6H9.5" />
    </>
  ),
  warning: (
    <>
      <path d="M8 2 14 13H2z" />
      <path d="M8 6v3.5M8 11.5v.5" />
    </>
  ),
  check: (
    <>
      <path d="M3 8.5 6.5 12 13 4.5" />
    </>
  ),
  user: (
    <>
      <circle cx="8" cy="5" r="3" />
      <path d="M2.5 14c0-3 2.5-5 5.5-5s5.5 2 5.5 5" />
    </>
  ),
  box: (
    <>
      <path d="M2.5 5 8 2l5.5 3v6L8 14l-5.5-3z" />
      <path d="M2.5 5 8 8l5.5-3M8 8v6" />
    </>
  ),
  edit: (
    <>
      <path d="M3 13.5 4 10l7.5-7.5 2 2L6 12z" />
      <path d="M10 4.5l2 2" />
    </>
  ),
  trash: (
    <>
      <path d="M3 4.5h10" />
      <path d="M6 2.5h4M5 4.5V14h6V4.5" />
      <path d="M7 7v4M9 7v4" />
    </>
  ),
  arrowLeft: (
    <>
      <path d="M13 8H3" />
      <path d="M7 4 3 8l4 4" />
    </>
  ),
  arrowRight: (
    <>
      <path d="M3 8h10" />
      <path d="m9 4 4 4-4 4" />
    </>
  ),
  clock: (
    <>
      <circle cx="8" cy="8" r="6" />
      <path d="M8 4.5V8l2.5 2" />
    </>
  ),
  close: (
    <>
      <path d="M3 3l10 10M13 3 3 13" />
    </>
  ),
  info: (
    <>
      <circle cx="8" cy="8" r="6" />
      <path d="M8 7v4M8 4.5v.5" />
    </>
  ),
};

const Icon = ({ name, size = 16, className = "", title }) => {
  const path = paths[name];

  if (!path) return null;

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 16 16"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.4"
      strokeLinecap="square"
      strokeLinejoin="miter"
      className={className}
      aria-hidden={title ? undefined : true}
      role={title ? "img" : undefined}
    >
      {title ? <title>{title}</title> : null}
      {path}
    </svg>
  );
};

export default Icon;
