AJS.toInit(function() {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

  function deleteConfig(row) {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "DELETE",
      contentType : "application/json",
      data : '{ "sla_token": "' + row +'" }',
      processData : false
    });
  }
  function updateConfig(row) {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "PUT",
      contentType : "application/json",
      data : '{ "sla": "' + AJS.$("#row"+row+"-edit input[name='sla']").val()
          + '","sla_token":"'
          + AJS.$("#row"+row+"-edit input[name='sla_token']").val()
          + '","projects":[' + AJS.$("#row"+row+"-edit select").val() + '] }',
      processData : false,
      success: function() {
        window.location.reload();
      }
    });
  }
  function createConfig() {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "POST",
      contentType : "application/json",
      data : '{ "sla": "' + AJS.$("#newRecord input[name='sla']").val()
          + '","sla_token":"'
          + AJS.$("#newRecord input[name='sla_token']").val()
          + '","projects":[' + AJS.$("#newRecord select").val() + '] }',
      processData : false,
      success: function() {
        window.location.reload();
      }
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
    AJS.$("#row" + row + "-show").hide();
    AJS.$("#row" + row + "-edit").show();
  });
  
  AJS.$("#realsla .sla-delete").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).data("row");
    deleteConfig(row);
    AJS.$("#row" + row + "-show").remove();
    AJS.$("#row" + row + "-edit").remove();
  });
  
  AJS.$("#realsla .row-cancel").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).data("row");
    AJS.$("#row" + row + "-show").show();
    AJS.$("#row" + row + "-edit").hide();
  });

  AJS.$("#newRecord input[type='submit']").click(function(e) {
    e.preventDefault();
    createConfig();
  });
  
  AJS.$("#realsla .row-level input[type='submit']").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).data("row");
    updateConfig(row);
  });
});