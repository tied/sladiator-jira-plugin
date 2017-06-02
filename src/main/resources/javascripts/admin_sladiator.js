AJS.toInit(function() {
  var baseUrl = AJS.$("#baseURL").val();
  
  function saveConfig() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/admin",
      type : "POST",
      contentType : "application/json",
      data : "{\"service_url\":\""+AJS.$("#service_url").val()+"\"}",
      success : function(data, textStatus) {
        AJS.$("#sladiator-admin .warningBox").hide();
        AJS.$("#sladiator-admin .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#sladiator-admin .infoBox").hide();
        AJS.$("#sladiator-admin .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function testConfig() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/test_service",
      type : "POST",
      contentType : "application/json",
      data : "{\"service_url\":\""+AJS.$("#service_url").val()+"\"}",
      success : function(data, textStatus) {
        AJS.$("#sladiator-admin .warningBox").hide();
        AJS.$("#sladiator-admin .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#sladiator-admin .infoBox").hide();
        AJS.$("#sladiator-admin .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function visitSLA() {
    window.open(AJS.$("#service_url").val());
  }
  
  AJS.$("#saveSLA").click(function(e) {
    e.preventDefault();
    saveConfig();
  });
  AJS.$("#testSLA").click(function(e) {
    e.preventDefault();
    testConfig();
  });
  AJS.$("#visitSLA").click(function(e) {
    e.preventDefault();
    visitSLA();
  });
});