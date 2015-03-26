var util = require('util');
var FS = require("q-io/fs");
var Handlebars = require("handlebars");

var dataset = {
  templates: {
    helper: null,
    template: null,
  },
  data: {
    template: null,
  }
};

var loading = [];

var warmupTemplate = "{{#this}} will not show {{missingContent}}{{! and comment}} "
      + "but can expand this {{text}} {{else}}{{that}}{{/this}}"
      + "{{#list}}found {{element}} in list {{/list}}";
var warmupData = ({"text": "some string", "list": [{"element": 1}, {"element": 2}, {"element": 3}, {"element": 4}]});

loading = loading.concat(Object.keys(dataset.templates).map(function(k) {
  return FS.read("../data/" + k + ".hbs").then(function(content) {
    return new Promise(function(r, f){
      dataset.templates[k] = content;
      r(true);
    });
  });
}));

loading = loading.concat(Object.keys(dataset.data).map(function(k) {
  return FS.read("../data/" + k + ".json").then(function(content) { 
    return new Promise(function(r, f){ 
      dataset.data[k] = JSON.parse(content);
      r(true); 
    })
  });
}));


var hrtNano = function(hrt) {
  return hrt[0] * 1e9 + hrt[1];
}

var measure = function(statsSet, setName, warmup, c) {

  {
    var start = process.hrtime();
    c();
    statsSet[setName]["cold"] = hrtNano(process.hrtime(start));
  }
  
  for (var i = 0; i < 1000; i++) {
    // warm up
    warmup();
  }
  
  {
    var iterations = 5;
    var measures = new Array(iterations);
    var start = process.hrtime();

    for (var i = 0; i < iterations; i++) {
      var setpStart = process.hrtime();
      c();
      measures[i] = process.hrtime(setpStart);
    }
    statsSet[setName]["avg"] = (hrtNano(process.hrtime(start))/iterations);

    var max = measures.map(hrtNano).reduce(function(a, b) { return a < b ? b : a}, Number.MIN_VALUE);
    var min = measures.map(hrtNano).reduce(function(a, b) { return a > b ? b : a}, Number.MAX_VALUE);

    statsSet[setName]["delta"] = (max - min);
  }
};

Promise.all(loading).then(function(allLoaded) {
  var allStats = {};

  Object.keys(dataset.templates).forEach(function(k) {
    var v = dataset.templates[k];

    var stats = (allStats[k] = {});
    stats["compile"] = {};
    stats["expansion"] = {};


    try {
      measure(stats, "compile", function() { Handlebars.compile(warmupTemplate) }, function() { Handlebars.compile(v) });

      var template = Handlebars.compile(v);
      var warmpup = Handlebars.compile(warmupTemplate);
      measure(stats, "expansion", function() { warmpup(warmupData) }, function() { template(dataset.data.template) });

    } catch (e) {
      console.log(e);
    }


  });

  var padder = "                          ";
  var numPad = "            ";

  Object.keys(allStats).forEach(function(suiteName) {
    var suite = allStats[suiteName];

    Object.keys(suite).forEach(function(testName) {
      var test = suite[testName];

      var name = suiteName + " " + testName;
      var padded = name + padder.substring(Math.min(name.length, padder.length-1));
      var fixedAvg = Math.round(test.avg).toString();
      var avgPadded = numPad.substring(Math.min(fixedAvg.length, numPad.length-1)) + fixedAvg;
      console.log("test " + padded + " ... bench: " + avgPadded + " ns/iter (+/- " + test.delta + ")" + " — cold " + test.cold + " ns");
    });


  });
  

}).catch(function(yo) {
  process.stdout("failure")
});