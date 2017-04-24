var path = {};
path.root = '/';

if (typeof Liferay !== 'undefined') //liferay context for views
    path.root = Liferay.ThemeDisplay.getPathContext() + '/';
else  // iframes
    path.root = '../'; // step back from root/learn-portlet to root (ex: root/learn-portlet/*.html)

path.api = {
    prefix: 'delegate/'
};
path.api.certificates = path.api.prefix + 'certificates/';
path.api.certificateStates = path.api.prefix + 'certificate-states/';
path.api.category = path.api.prefix + 'categories/';
path.api.lesson = path.api.packages = path.api.prefix + 'packages/';
path.api.lessonResults = path.api.prefix + 'lesson-results/';
path.api.files = path.api.prefix + 'files/';
path.api.gradebooks = path.api.prefix + 'gradebooks/';
path.api.lessongrades = path.api.prefix + 'lesson-grades/';
path.api.teacherGrades = path.api.prefix + 'teacher-grades/';
path.api.notifications = path.api.prefix + 'notifications/';
path.api.courses = path.api.prefix + 'courses/';
path.api.users = path.api.prefix + 'users/';
path.api.organizations = path.api.prefix + 'organizations/';
path.api.administering = path.api.prefix + 'administering/';
path.api.questions = path.api.prefix + 'questions/';
path.api.plainText = path.api.prefix + 'plaintext/';
path.api.print = path.api.prefix + 'print/';

path.api.activityToStatement = path.api.prefix + 'activityToStatement/';
path.api.manifestactivities =  path.api.prefix + 'manifestactivities/';
path.api.report =  path.api.prefix + 'report/';
path.api.liferay = path.api.prefix  + 'liferay/';
path.api.lrs2activity = path.api.prefix  + 'lrs2activity-filter-api-controller/';
path.api.uri = path.api.prefix  + 'uri/';

path.api.tags = path.api.prefix + 'tags/';

path.api.slideSets = path.api.prefix + 'slide-sets/';
path.api.slides = path.api.prefix + 'slides/';
path.api.slideElements = path.api.prefix + 'slide-elements/';
path.api.slideThemes = path.api.prefix + 'slide-themes/';
path.api.slideDevices = path.api.prefix + 'devices/';
path.api.contentProviders = path.api.prefix + 'content-providers/';

path.api.assignment = path.api.prefix + 'assignments/';

path.sequencing = path.api.prefix + 'sequencing/';
path.scormorganizations = path.api.prefix + 'scormorganizations/';
path.rte = path.api.prefix + 'rte/';

path.api.activities = path.api.prefix + 'activities/';
path.api.statements = path.api.prefix + 'statements/';

path.api.competences = {};
path.api.competences.category = path.api.prefix + 'competencecategory';
path.api.competences.level = path.api.prefix + 'competencelevel';
path.api.competences.skill = path.api.prefix + 'competenceskill';
path.api.competences.competence = path.api.prefix + 'competence';
path.api.competences.experience = path.api.prefix + 'competencecertificate';
path.api.competences.usersWithSkills = path.api.prefix + 'competence/user-summary';
path.api.competences.UsersWithoutSkills = path.api.prefix + 'competence/user-summary/empty-skills';

path.api.stories = path.api.prefix + 'storyTrees/';

path.api.valamisActivityLike = path.api.prefix + 'activity-like/';
path.api.valamisActivityComment = path.api.prefix + 'activity-comment/';

path.api.exportData = path.api.prefix + 'export/';

path.api.packageExport = function(id) {};

path.api.urlCheck = path.api.prefix + 'url/check/';

path.api.dashboard = path.api.prefix + 'dashboard/';

path.api.transcript = path.api.prefix + 'transcript/';

path.api.certificateGoals = path.api.prefix + 'certificate-goals/';

path.api.liferayArticle = path.api.prefix + 'liferay/article/';

path.api.learningReport = path.api.prefix + 'learning-report/';

path.api.trainingCalendars = path.api.prefix + 'training-calendars/';
path.api.trainingEvents = path.api.prefix + 'training-events/';