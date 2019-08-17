import angular from 'angular';
import uiRouter from 'angular-ui-router';
import breadcrumbsPatientListComponent from './components/breadcrumbsPatientList.component';
import breadcrumbsEncounterListComponent from './components/breadcrumbsEncounterList.component';
import breadcrumbsUserListComponent from './components/breadcrumbsUserList.component';
import headerComponent from './components/header.component.js';
import patientsComponent from './components/patients.component.js';
import usersComponent from './components/users.component.js';
import encountersComponent from './components/encounters.component.js';
import 'openmrs-contrib-uicommons';
import 'angular-chart.js';

let homeModule = angular.module('home', [ uiRouter, 'openmrs-contrib-uicommons','chart.js'])
    .config(($stateProvider, $urlRouterProvider) => {
        "ngInject";
        $urlRouterProvider.otherwise('/');

        $stateProvider.state('home', {
            url: '/',
            template: require('./usersPage.html')
        });

        $stateProvider.state('showUsersData', {
        url: '/user-list',
        template: require('./usersPage.html')
        });

        $stateProvider.state('showPatientsData', {
            url: '/patient-list',
            template: require('./patientsPage.html')
        });

        $stateProvider.state('showEncountersData', {
            url: '/encounter-list',
            template: require('./encountersPage.html')
        });
    })
    // To prevent adding Hash bangs(#!/) instead of simple hash(#/) in Angular >1.5

    .config(['$locationProvider', function($locationProvider) {
      $locationProvider.hashPrefix('');
    }])

    .component('breadcrumbsPatientListComponent', breadcrumbsPatientListComponent)
    .component('breadcrumbsEncounterListComponent', breadcrumbsEncounterListComponent)
    .component('breadcrumbsUserListComponent', breadcrumbsUserListComponent)
    .component('headerComponent', headerComponent)
    .component('encountersComponent', encountersComponent )
    .component('usersComponent', usersComponent )
    .component('patientsComponent', patientsComponent );

export default homeModule;
