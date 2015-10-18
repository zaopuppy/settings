

$(function() {
  // hi
  console.log('start');

  // id, title, notes, notify-time
  var Todo = Backbone.Model.extend({
    urlRoot: '/todo',

    defaults: function() {
      return {
        title: 'empty todo...',
        completed: false,
        notes : '',
      };
    },

    initialize: function() {
      console.log("Todo.initialize()");
    }

  });

  // var TodoList = Backbone.Collection.extend({
  //   model: Todo,
  // });

  var TodoView = Backbone.View.extend({
    tagName: 'li',

    template: _.template($('#item-template').html()),

    events: {
      'click': 'on_click',
    },

    on_click: function() {
      console.log('onclick');
    },

    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
    },

    initialize: function() {
      console.log("TodoView.initialize()");
    }
  });

  var model = new Todo;
  var view = new TodoView({
    model: model,
  })

  // var AppView = Backbone.View.extend({
  //   el: $('#app-view'),
  // });

  // var todo_list = new TodoList;
});


// END
