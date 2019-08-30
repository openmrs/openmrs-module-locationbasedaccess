import angular from 'angular';
import uiRouter from 'angular-ui-router';
import mainComponent from './main.component.js';
import Home from '../home/home';
import uicommons from 'openmrs-contrib-uicommons';

let OpenmrsModuleLocationbasedModule = angular.module('OpenmrsModuleLocationbased', [ uiRouter, Home.name, 'openmrs-contrib-uicommons'
    ])
    .component('main', mainComponent);

export default OpenmrsModuleLocationbasedModule;
