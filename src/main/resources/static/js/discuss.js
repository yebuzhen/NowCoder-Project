$(function () {

  $("#topBtn").click(setTop);
  $("#wonderfulBtn").click(setWonderful);
  $("#deleteBtn").click(SetDelete);

})

function like(btn, entityType, entityId, entityUserId, postId) {

  $.post (

      CONTEXT_PATH + "/like",
      {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
      function (data) {

        data = $.parseJSON(data);

        if (data.code == 0) {

          $(btn).children("i").text(data.likeCount);
          $(btn).children("b").text(data.likeStatus == 1 ? 'Liked' : 'Like');

        } else {
          alert(data.msg + "from jS");
        }

      }

  )

}

// Set to the top
function setTop() {

  $.post(

      CONTEXT_PATH + "/discuss/top",
      {"id":$("#postId").val()},

      function (data) {

        data = $.parseJSON(data);

        if (data.code == 0) {
          $("#topBtn").attr("disabled", "disabled");
        } else {
          alert(data.msg);
        }

      }

  )

}

// Set as wonderful
function setWonderful() {

  $.post(

      CONTEXT_PATH + "/discuss/wonderful",
      {"id":$("#postId").val()},

      function (data) {

        data = $.parseJSON(data);

        if (data.code == 0) {
          $("#wonderfulBtn").attr("disabled", "disabled");
        } else {
          alert(data.msg);
        }

      }

  )

}

// Delete
function SetDelete() {

  $.post(

      CONTEXT_PATH + "/discuss/delete",
      {"id":$("#postId").val()},

      function (data) {

        data = $.parseJSON(data);

        if (data.code == 0) {
          location.href = CONTEXT_PATH + "/index";
        } else {
          alert(data.msg);
        }

      }

  )

}








