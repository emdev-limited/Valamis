/**
 * Created by Iliya Tryapitsin on 18.08.2014.
 */

var path = {};
path.root = '/';

if (typeof Liferay !== 'undefined') //liferay context for views
    path.root = Liferay.ThemeDisplay.getPathContext() + '/';
else  // iframes
    path.root = '../'; // step back from root/learn-portlet to root (ex: root/learn-portlet/*.html)

path.portlet = {
    prefix: 'learn-portlet/'
};

path.api = {
    prefix: 'delegate/'
};
path.api.certificates = path.api.prefix + 'certificates/';
path.api.certificateStates = path.api.prefix + 'certificate-states/';
path.api.category = path.api.prefix + 'categories/';
path.api.lesson = path.api.packages = path.api.prefix + 'packages/';
path.api.files = path.api.prefix + 'files/';
path.api.gradebooks = path.api.prefix + 'gradebooks/';
path.api.notifications = path.api.prefix + 'notifications/';
path.api.courses = path.api.prefix + 'courses/';
path.api.users = path.api.prefix + 'users/';
path.api.organizations = path.api.prefix + 'organizations/';
path.api.administrering = path.api.prefix + 'administering/';
path.api.questions = path.api.prefix + 'questions/';
path.api.plainText = path.api.prefix + 'plaintext/';
path.api.print = path.api.prefix + 'print/';

path.api.settingsApi = path.api.prefix + 'settings-api-controller/';
path.api.manifestactivities =  path.api.prefix + 'manifestactivities/';
path.api.report =  path.api.prefix + 'report/';
path.api.liferay = path.api.prefix  + 'liferay/';
path.api.lrs2activity = path.api.prefix  + 'lrs2activity-filter-api-controller/';
path.api.uri = path.api.prefix  + 'uri/';

path.api.tags = path.api.prefix + 'tags/';

path.api.slideSets = path.api.prefix + 'slidesets/';
path.api.slides = path.api.prefix + 'slides/';
path.api.slideEntities = path.api.prefix + 'slideentities/';
path.api.slideThemes = path.api.prefix + 'slidethemes/';
path.api.slideDevices = path.api.prefix + 'devices/';

path.sequencing = path.api.prefix + 'sequencing/';
path.rte = path.api.prefix + 'rte/';

path.api.tincanApi = path.portlet.prefix + 'xapi/'; // do not remove!
path.api.activities = path.api.prefix + 'activities/';
path.api.statements = path.api.prefix + 'statements/';

path.api.valamisActivityLike = path.api.prefix + 'activity-like/';
path.api.valamisActivityComment = path.api.prefix + 'activity-comment/';

path.api.packageExport = function(id) {

};

path.api.dashboard = path.api.prefix + 'dashboard/';
