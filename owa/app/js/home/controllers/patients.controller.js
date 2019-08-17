class PatientsController {
  /* @ngInject */
  constructor($http, $window) {

    var vm = this;
    var location = $window.location.toString();
    var serverLocation = location.substring(0, location.indexOf('/owa/')); // http://localhost:8080/openmrs
    vm.labels = [];
    vm.series = ['Patients'];
    vm.data = [];
    $http({
      method : 'GET',
      url:serverLocation+"/ws/rest/v1/lbac/locationwise-patients-count"
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
      for( var i in response.data.results){
        vm.labels.push(i);
        vm.data.push(response.data.results[i]);
      }
    }, function errorCallback(response) {
      console.log('An Error occurred with status : ' + response.status);
    });


  }
}

export default PatientsController;
