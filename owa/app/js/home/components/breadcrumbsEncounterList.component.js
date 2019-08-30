import template from '../breadcrumbs.html';
import controller from '../controllers/breadcrumbsEncounterList.controller';

let breadcrumbsEncounterListComponent = {
    restrict: 'E',
    bindings: {},
    template: template,
    controller: controller,
    controllerAs: 'vm'
};

export default breadcrumbsEncounterListComponent;
