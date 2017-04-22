var LearningTranscript = Marionette.Application.extend({
  channelName: 'learningTranscript',
  initialize: function() {
    this.addRegions({
      mainRegion: '#learningTranscriptAppRegion'
    });
  },
  onStart: function(options) {
    _.extend(this, options);
    this.execute('show:user:transcript', Utils.getUserId());
  }
});

var learningTranscript = new LearningTranscript();

// handlers

learningTranscript.commands.setHandler('show:user:transcript', function(userId){
  learningTranscript.selectedUserId = userId;
  var layoutView = new learningTranscript.Views.AppLayoutView();
  learningTranscript.mainRegion.show(layoutView);
});