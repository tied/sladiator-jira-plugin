AJS.toInit(function() {
  
  var baseUrl = AJS.$("#baseURL").val();
  var serviceUrl = AJS.$("#serviceURL").val();
  
  function startProgress() {
    AJS.$("#freezer").show();
  }
  
  function stopProgress() {
    AJS.$("#freezer").hide();
  }
  
  function formToJSON(form)
  {
    return "{"+AJS.$(form).serializeArray().map(function(i){return '"'+i.name+'":'+'"'+escape(i.value)+'"'}).join(",")+"}";
  };
  
  function maintainSLA() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/config",
      type : "POST",
      contentType : "application/json",
      data : formToJSON("#sladiator-project"),
      beforeSend : function(jqXHR, settings) {
        startProgress();
      },
      success : function(data, textStatus) {
        stopProgress();
        AJS.$("#sladiator-project .warningBox").hide();
        AJS.$("#sladiator-project .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        stopProgress();
        AJS.$("#sladiator-project .infoBox").hide();
        AJS.$("#sladiator-project .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function visitSLA() {
    token = AJS.$("#sla_token").val();
    if ( token != "") {
      window.open(serviceUrl+"/sla_token/"+ token);
    }
  }
  
  function teleportSLA() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/teleport",
      type : "POST",
      contentType : "application/json",
      data : formToJSON("#sladiator-teleprot"),
      beforeSend : function(jqXHR, settings) {
        startProgress();
      },
      success : function(data, textStatus) {
        stopProgress();
        AJS.$("#sladiator-teleprot .warningBox").hide();
        AJS.$("#sladiator-teleprot .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        stopProgress();
        AJS.$("#sladiator-teleprot .infoBox").hide();
        AJS.$("#sladiator-teleprot .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  function janitorSLA() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/janitor",
      type : "POST",
      contentType : "application/json",
      data : formToJSON("#sladiator-janitor"),
      beforeSend : function(jqXHR, settings) {
        startProgress();
      },
      success : function(data, textStatus) {
        stopProgress();
        window.location.reload();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        stopProgress();
        AJS.$("#sladiator-janitor .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  function deleteSLA() {
    jQuery.fn.isDirty = function () {return false;}
    var answer = confirm("Please confirm that you wish to delete SLAdiator configuration for your project!");
    if (answer) {
      AJS.$.ajax({
        url : baseUrl + "/rest/sladiator/1.0/config",
        type : "DELETE",
        contentType : "application/json",
        data : formToJSON("#sladiator-delete"),
        success : function(data, textStatus) {
          window.location.reload();
        }
      });
    }
  }
  AJS.$("#saveSLA").click(function(e) {
    e.preventDefault();
    maintainSLA();
  });
  AJS.$("#visitSLA").click(function(e) {
    e.preventDefault();
    visitSLA();
  });
  
  AJS.$("#teleportSLA").click(function(e) {
    e.preventDefault();
    teleportSLA();
  });
  AJS.$("#janitorSLA").click(function(e) {
    e.preventDefault();
    janitorSLA();
  });
  AJS.$("#deleteSLA").click(function(e) {
    e.preventDefault();
    deleteSLA();
  });
});