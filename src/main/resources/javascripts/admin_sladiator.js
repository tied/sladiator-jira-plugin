AJS.toInit(function() {
  var baseUrl = AJS.$("#baseURL").val();
  var indicator = new JIRA.Dialog();
  
  function startProgress() {
    AJS.$("#freezer").show();
    indicator._showloadingIndicator();
  }
  
  function stopProgress() {
    AJS.$("#freezer").hide();
    indicator._hideloadingIndicator();
  }
  
  function admSaveConfig() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/admin",
      type : "POST",
      contentType : "application/json",
      data : "{\"service_url\":\""+AJS.$("#service_url").val()+"\"}",
      beforeSend : function(jqXHR, settings) {
        startProgress();
      },
      success : function(data, textStatus) {
        stopProgress();
        AJS.$("#sladiator-admin .warningBox").hide();
        AJS.$("#sladiator-admin .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        stopProgress();
        AJS.$("#sladiator-admin .infoBox").hide();
        AJS.$("#sladiator-admin .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function admTestConfig() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/test_service",
      type : "POST",
      contentType : "application/json",
      data : "{\"service_url\":\""+AJS.$("#service_url").val()+"\"}",
      beforeSend : function(jqXHR, settings) {
        startProgress();
      },
      success : function(data, textStatus) {
        stopProgress();
        AJS.$("#sladiator-admin .warningBox").hide();
        AJS.$("#sladiator-admin .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        stopProgress();
        AJS.$("#sladiator-admin .infoBox").hide();
        AJS.$("#sladiator-admin .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function admVisitSLA() {
    window.open(AJS.$("#service_url").val());
  }
  
  AJS.$("#adm_saveSLA").click(function(e) {
    e.preventDefault();
    admSaveConfig();
  });
  AJS.$("#adm_testSLA").click(function(e) {
    e.preventDefault();
    admTestConfig();
  });
  AJS.$("#adm_visitSLA").click(function(e) {
    e.preventDefault();
    admVisitSLA();
  });
});