// Empty constructor
function InputFileSelector() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
InputFileSelector.prototype.show = function(message, duration, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'InputFileSelector', 'show', [options]);
}

// Installation constructor that binds InputFileSelector to window
InputFileSelector.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.fileSelector = new InputFileSelector();
  return window.plugins.inputFileSelector;
};
cordova.addConstructor(InputFileSelector.install);