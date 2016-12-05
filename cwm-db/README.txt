cwm-db is a subset of Databinder, including only the functionality that we've actually used.

Classes we use in cwm-parent:

net.databinder.hib.Databinder
net.databinder.models.hib.HibernateObjectModel
net.databinder.models.hib.HibernateListModel
net.databinder.models.hib.HibernateProvider
net.databinder.models.hib.SortableHibernateProvider
net.databinder.components.hib.DataForm
net.databinder.hib.DataRequestCycle
net.databinder.models.PropertyDataProvider

interfaces we use:

net.databinder.models.hib.ICriteriaBuilder
net.databinder.models.hib.OrderingCriteriaBuilder
net.databinder.models.hib.QueryBuilder
net.databinder.hib.SessionUnit

also used; separate out to a separate authorization module?:

net.databinder.auth.data.DataUser
net.databinder.auth.hib.AuthDataApplication
net.databinder.auth.hib.AuthDataSession
net.databinder.auth.AuthDataSessionBase

used, but may want to drop

net.databinder.components.hib.DataBrowser  (removed)
net.databinder.auth.data.hib.BasicPassword
net.databinder.auth.components.RSAPasswordTextField;  (removed)
net.databinder.auth.valid.EqualPasswordConvertedInputValidator;

maybe we should start using?:

net.databinder.models.Models
net.databinder.models.PropertyDataProvider
net.databinder.components.hib.DataPanel  (+ other panel types?)


