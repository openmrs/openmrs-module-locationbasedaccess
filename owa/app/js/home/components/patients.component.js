import template from '../common-table.html';
import controller from '../controllers/patients.controller';

let patientsComponent = {
  restrict: 'E',
  bindings: {},
  controller: controller,
  controllerAs: 'vm',
  template: template
};

export default patientsComponent;
