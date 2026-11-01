import Icon from './Icon';

const PageHeader = ({ title, subtitle, icon, breadcrumbs, actions }) => (
  <div className="page-head">
    <div className="page-head__text">
      {breadcrumbs ? <div className="breadcrumbs">{breadcrumbs}</div> : null}
      <h1 className="page-title">
        {icon ? <Icon name={icon} size={18} /> : null}
        {title}
      </h1>
      {subtitle ? <p className="page-subtitle">{subtitle}</p> : null}
    </div>
    {actions ? <div className="page-actions">{actions}</div> : null}
  </div>
);

export default PageHeader;
