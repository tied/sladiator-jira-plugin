AJS.toInit(function() {
  var baseUrl = AJS.$("#baseURL").val();

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
      success : function(data, textStatus) {
        AJS.$("#sladiator-project .warningBox").hide();
        AJS.$("#sladiator-project .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#sladiator-project .infoBox").hide();
        AJS.$("#sladiator-project .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  
  function testSLA() {
    jQuery.fn.isDirty = function () {return false;}
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator/1.0/connection",
      type : "POST",
      contentType : "application/json",
      data : formToJSON("#sladiator-project"),
      beforeSend : function(jqXHR, settings) {
        AJS.$("#freezer").show();
      },
      success : function(data, textStatus) {
        AJS.$("#freezer").hide();
        AJS.$("#sladiator-project .warningBox").hide();
        AJS.$("#sladiator-project .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#freezer").hide();
        AJS.$("#sladiator-project .infoBox").hide();
        AJS.$("#sladiator-project .warningBox").html(jqXHR.responseText).show();
      }
    });
  }
  function visitSLA() {
    token = AJS.$("#sla_token").val();
    if ( token != "") {
      window.open("https://simplesla.ebit.lv/sla_token/"+ token);
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
        AJS.$("#freezer").show();
      },
      success : function(data, textStatus) {
        AJS.$("#freezer").hide();
        AJS.$("#sladiator-teleprot .warningBox").hide();
        AJS.$("#sladiator-teleprot .infoBox").html(data).show();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#freezer").hide();
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
        AJS.$("#freezer").show();
      },
      success : function(data, textStatus) {
        AJS.$("#freezer").hide();
        window.location.reload();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#freezer").hide();
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
  AJS.$("#testSLA").click(function(e) {
    e.preventDefault();
    testSLA();
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