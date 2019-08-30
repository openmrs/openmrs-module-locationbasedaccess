import template from '../common-table.html';
import controller from '../controllers/users.controller';

let usersComponent = {
    restrict: 'E',
    bindings: {},
    controller: controller,
    controllerAs: 'vm',
    template: template
};

export default usersComponent;
