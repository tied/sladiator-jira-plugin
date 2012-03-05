AJS.toInit(function() {
  var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

  function teleport() {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-teleport/1.0/teleport/",
      type : "POST",
      contentType : "application/json",
      data : '{ "date_from": "' + AJS.$("#date_from").val() + '" }',
      processData : true,
      beforeSend : function(jqXHR, settings) {
        AJS.$("#freezer").show();
      },
      success : function(data, textStatus) {
        AJS.$("#freezer").hide();
        if (data != "") {
          AJS.$(".teleport .formErrors").hide();
          AJS.$(".teleport .success").html(data.split(".").map(function(e) {
            return "<span>" + e + "</span>"
          }).join("")).show();
        } else {
          AJS.$(".teleport .formErrors").hide();
        }

      },
      error : function(jqXHR, textStatus, errorThrown) {
        AJS.$("#freezer").hide();
        AJS.$(".teleport .success").hide();
        AJS.$(".teleport .formErrors").html(jqXHR.responseText.split(".").map(function(e) {
          return "<span class='errMsg'>" + e + "</span>"
        }).join("")).show();
      }
    });
  }

  function deleteConfig(row) {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "DELETE",
      contentType : "application/json",
      data : '{ "sla_token": "' + row + '" }',
      processData : false
    });
  }
  function updateConfig(row) {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "PUT",
      contentType : "application/json",
      data : '{ "sla": "'
          + AJS.$("#row" + row + "-edit input[name='sla']").val()
          + '","sla_token":"'
          + AJS.$("#row" + row + "-edit input[name='sla_token']").val()
          + '", "send_assignee":"'
          + (AJS.$("#row" + row + "-edit input[name='send_assignee']").is(
              ':checked') ? 1 : 0) + '","projects":['
          + AJS.$("#row" + row + "-edit select").val() + '] }',
      processData : false,
      success : function(data, textStatus) {
        if (data != "") {
          AJS.$("#row" + row + "-edit .formErrors").html(
              data.split(".").map(function(e) {
                return "<span class='errMsg'>" + e + "</span>"
              }).join("")).show();
        } else {
          window.location.reload();
        }
      }
    });
  }
  function createConfig() {
    AJS.$.ajax({
      url : baseUrl + "/rest/realsla-config/1.0/",
      type : "POST",
      contentType : "application/json",
      data : '{ "sla": "'
          + AJS.$("#newRecord input[name='sla']").val()
          + '","sla_token":"'
          + AJS.$("#newRecord input[name='sla_token']").val()
          + '", "send_assignee":"'
          + (AJS.$("#newRecord input[name='send_assignee']").is(':checked') ? 1
              : 0) + '","projects":[' + AJS.$("#newRecord select").val()
          + '] }',
      processData : false,
      success : function(data, textStatus) {
        if (data != "") {
          AJS.$("#newRecord .formErrors").html(data.split(".").map(function(e) {
            return "<span class='errMsg'>" + e + "</span>"
          }).join("")).show();
        } else {
          window.location.reload();
        }
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
    var row = AJS.$(this).attr("data-row");
    AJS.$("#row" + row + "-show").hide();
    AJS.$("#row" + row + "-edit").show();
  });

  AJS.$("#realsla .sla-delete").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).attr("data-row");
    deleteConfig(row);
    AJS.$("#row" + row + "-show").remove();
    AJS.$("#row" + row + "-edit").remove();
  });

  AJS.$("#realsla .row-cancel").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).attr("data-row");
    AJS.$("#row" + row + "-show").show();
    AJS.$("#row" + row + "-edit").hide();
  });

  AJS.$("#newRecord input[type='submit']").click(function(e) {
    e.preventDefault();
    createConfig();
  });

  AJS.$("#realsla .row-level input[type='submit']").click(function(e) {
    e.preventDefault();
    var row = AJS.$(this).attr("data-row");
    updateConfig(row);
  });

  AJS.$(".teleport input[type='submit']").click(function(e) {
    e.preventDefault();
    teleport();
  });

});