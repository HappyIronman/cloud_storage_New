import Vue from 'vue'
import VueResource from 'vue-resource'

Vue.use(VueResource);

export function requestApi(method, path, params, callback) {
  var baseUrl = 'http://localhost:8081/data/';
  var requestUri = baseUrl + path;
  var responseData = '';
  if (method === 'get') {
    // GET /someUrl
    return Vue.http.get(requestUri, {params: params}).then(response => {
      // get body data
      responseData = response.body;
      console.log("ResponseData: " + JSON.stringify(responseData));
      return callback(responseData);
    }).catch(response => {
      // error callback
      console.error("request ERROR! " + JSON.stringify(response));
    });
  }

  else if (method === 'post') {
    return Vue.http.post(requestUri, params).then((response) => {
      // success callback
      responseData = response.data;
      console.log("ResponseData: " + JSON.stringify(responseData));
      return callback(responseData);
    }).catch((response) => {
      console.error("request ERROR! " + JSON.stringify(response));
      alert(response.body.msg)
      // error callback
    });
  }

  else if (method === 'put') {
    return Vue.http.put(requestUri, params).then((response) => {
      // success callback
      responseData = response.data;
      console.log("ResponseData: " + responseData);
      return callback(responseData);
    }).catch((response) => {
      console.error("request ERROR! " + JSON.stringify(response));
      // error callback
    });
  }

  else if (method === 'delete') {
    return Vue.http.delete(requestUri, {params: params}).then(response => {
      // get body data
      responseData = response.body;
      console.log("ResponseData: " + responseData);
      return callback(responseData);
    }).catch(response => {
      // error callback
      console.error("request ERROR! " + JSON.stringify(response));
    });
  }

  else {
    console.error("request method not supported! " + method);
    return;
  }

  return responseData
}
