import template from '../breadcrumbs.html';
import controller from '../controllers/breadcrumbsPatientList.controller';

let breadcrumbsPatientListComponent = {
    restrict: 'E',
    bindings: {},
    template: template,
    controller: controller,
    controllerAs: 'vm'
};

export default breadcrumbsPatientListComponent;
