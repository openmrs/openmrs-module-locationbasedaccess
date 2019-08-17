import template from '../breadcrumbs.html';
import controller from '../controllers/breadcrumbsUserList.controller';

let breadcrumbsUserListComponent = {
    restrict: 'E',
    bindings: {},
    template: template,
    controller: controller,
    controllerAs: 'vm'
};

export default breadcrumbsUserListComponent;
