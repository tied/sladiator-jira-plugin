AJS.toInit(function() {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

  //    
  // function populateForm() {
  // AJS.$.ajax({
  // url: baseUrl + "/rest/realsla-config/1.0/",
  // dataType: "json",
  // success: function(config) {
  // AJS.$("#newRecord input[name='sla']").attr("value", config.sla);
  // AJS.$("#newRecord input[name='sla_token']").attr("value",
  // config.sla_token);
  // }
  // });
  // }
  //    
  function updateConfig() {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "PUT",
      contentType : "application/json",
      // , #newRecord select
      // data: JSON.stringify(AJS.$("#newRecord
      // input[type='text']").serializeArray()[0]),
      // { "sla": "Sample", "sla_token": "one2" }
      data : '{ "sla": "' + AJS.$("#newRecord input[name='sla']").attr("value")
          + '", "sla_token": "'
          + AJS.$("#newRecord input[name='sla_token']").attr("value") + '" }',
      processData : false
    });
  }
  //    
  // populateForm();
  //    
  AJS.$("#add_sla").click(function(e) {
    e.preventDefault();
    AJS.$("#newRecord").show();
  });

  AJS.$("#realsla .new-cancel").click(function(e) {
    e.preventDefault();
    AJS.$(this).parent().parent().parent().hide();
  });

  AJS.$("#realsla .sla-edit").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).data("row");
    AJS.$("#" + row + "-show").hide();
    AJS.$("#" + row + "-edit").show();
  });

  AJS.$("#realsla .row-cancel").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).data("row");
    AJS.$("#" + row + "-show").show();
    AJS.$("#" + row + "-edit").hide();
  });

  //    
  // AJS.$("#newRecord input[type='submit']").click(function(e) {
  // e.preventDefault();
  // updateConfig();
  // });
});