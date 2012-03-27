AJS.toInit(function() {
  var baseUrl = AJS.$("#baseURL").val();

  function formToJSON(form)
  {
      var o = {};
      var a = AJS.$(form).serializeArray();
      AJS.$.each(a, function() {
          if (o[this.name] !== undefined) {
              if (!o[this.name].push) {
                  o[this.name] = [o[this.name]];
              }
              o[this.name].push(this.value || '');
          } else {
              o[this.name] = this.value || '';
          }
      });
      return o;
  };
  
  function maintainSLA() {
    AJS.$.ajax({
      url : baseUrl + "/rest/sladiator-project/1.0/",
      type : "POST",
      contentType : "application/json",
      data : formToJSON("#sladiator-project")
    });
  }
  
  AJS.$("#saveSLA").click(function(e) {
    e.preventDefault();
    maintainSLA();
  });
});