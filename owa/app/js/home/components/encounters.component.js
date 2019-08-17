import template from '../common-table.html';
import controller from '../controllers/encounters.controller';

let encountersComponent = {
    restrict: 'E',
    bindings: {},
    controller: controller,
    controllerAs: 'vm',
    template: template
};

export default encountersComponent;
