# Valamis - eLearning for Liferay

**http://valamis.arcusys.com/**

[![build status](https://api.travis-ci.org/arcusys/Valamis.png)](http://travis-ci.org/arcusys/Valamis)

### This is Valamis CE version - No support - No warranty
To get supported Valamis Enterprise Edition contact us by http://valamis.arcusys.com/get-valamis
or purchase it straight from Liferay Marketplace http://www.liferay.com/marketplace/-/mp/application/35268197

#### The following features are not supported in the CE version:
* Competences
* Knowledge Map
* Mobile application
* Assignments
* Training Events
* Lesson Studio Beta

Valamis is a social learning environment for sharing and receiving knowledge. We want to enable people to share their knowledge and learn through digital technologies and user interaction. You can use Valamis as your organization's social learning environment.

Supported Liferay version is currently 6.2 and DXP 7.0 SP3.
The targeted version of SCORM is 2004 4th edition with support of SCORM 1.2.
All server-side code is written using the Scala programming language for the JVM.

The current implementation is able to display SCORM and Tin Can content with respect towards the different content organizations and the activity structure in each organization.
Application includes a question editor for creating quizes with different types of questions (single-/multi-choice, matching, short answer, etc.)

Administrative features let you manage SCORM packages, uploading them in the standard zipped format.
The user interface is available as JSR-compliant portlets, which may be deployed into Liferay portal. The portlet version has been tested on Liferay 6.1.1 and 6.1.2 and depends on its specific features.

The solution uses Liferay database, so no there is need to install an additional database.

If deployed against a portlet container, the end-user features are available via the portlet's standard View mode, while administrative features are available via the Edit mode. There are also separate portlets for question editor, quiz editor and gradebook.

### NOTE
Since version 3.4.1, Valamis Community Edition is separated into three packages on GitHub: Valamis LRS (Learning Record Store, https://github.com/arcusys/valamis-lrs),
Learning Paths (https://github.com/arcusys/learning-paths), Valamis components (this repository). You need to compile all of these.

### Download 
Download Valamis CE source code from this repository and compile the application yourself.

### Building
This is an sbt project.

#### Liferay 6.2
Go to Settings.scala and change the line #10   
`val liferay = Liferay620`

Run
`sbt -J-Xss8M -mem 4096 clean package'`

Deploy to the running Liferay instance
`sbt deploy`

#### Liferay DXP
Go to Settings.scala and change the line #10   

`val liferay = Liferay700`

Run

`sbt -J-Xss8M -mem 4096 clean osgiFullPackage'`

Deploy the package and all dependencies to the running Liferay instance

`sbt osgiFullDeploy`

### Known issues
**PermGen issue**: Valamis requires 512Mb of PermGen size. This is default size in Liferay bundled with glassfish, but permgen in Tomcat and jBoss bundles should be increased.

Liferay 6.1 EE bundled with Tomcat 7 can throw errors while accessing uploaded content. To avoid this problem, just turn off the GZip conmpression:
`com.liferay.portal.servlet.filters.gzip.GZipFilter=false`

If you change Settings.scala, you must run **clean** command!

If you have several tomcat instances running, specify liferay home dir in deploy and osgiFullDeploy commands:

`sbt deploy /opt/liferay-portal-6.2-ce-ga6`

`sbt osgiFullDeploy /opt/liferay-dxp-digital-enterprise-7.0-sp4`

## Version 3.4 Update 27.07.2017
 - A new portlet, called Certificate Expiration Tracker, gives the instructor new tools for notifying the users about expiring and expired certificates
 - Curriculum Manager and Viewer were merged into the Learning Paths portlet. This new logic implies that a learning path is a set of learning goals that you achieve in order to get a certificate. Some goals, like lessons, can be completed straight from the UI of the relevant learning path
 - It is now possible to export the Report data into JSON or CSV
 - New Report types: average passing grade and lesson attempts and completions
 - Valamis Administration portlet improvements: settings are saved for each instance separately, except for the license, which is saved in the main instance
 - Course manager site template option
 - Liferay DXP support for Valamis CE version
 - Dashboard improvements: links to Training Events and Assignments in Learning Paths, and possibility to go back to original page after viewing a goal
 - Lesson viewer: option to limit visible categories
 - New email notifications: training event start in one week, user added to training event
 - Valamis API
 
## Version 3.2 Update 21.04.2017
 - New Learning Pattern Report portlet that shows an interactive table with full learning data in a selected course
 - New Report Portlet that allows for different types of learning data reports: certificate report, lesson report, user report
 - New Learning Transcript portlet, that describes the overall learning progress of a student. It is possible to print out the transcript
 - Lesson auto-locking in Lesson Studio implemented to prevent simultaneous work on the same lesson
 - Improved design for questions and lesson summary in Lesson viewer
 - More details for Course portlets, like maximum number of participants, availability time, etc.
 - Email notifications for various actions and events

## Version 3.0 Update 02.08.2016
 - Improved Gradebook portlet UI and features
 - New Course Browser and Course Manager portlets
 - Curriculum Management improvements: grouped goals, optional goals
 - Initial Liferay DXP support and theme
 - Lesson viewer accessibility improvements
 - Lesson Studio UI: Grid option, Undo/Redo button, Font size for questions and text elements, vertical navigation setting.
 - Lesson versioning and visibility
 - Liferay activities show up in Valamis Activities portlet (configurable in Preferences)

## Version 2.6 Update 3.2.2016
 - Responsive content capabilites for three device types: mobile, tablet and desktop.
 - Liferay articles as text elements
 - Slide title and studio element management
 - Lesson manual arrangement
 - Import questions from Moodle to Valamis

## Version 2.4 Update 26.6.2015
 - New Valamis login page with new design
 - Valamis site automatically created with Dashboard page when application is deployed
 - Content management portlet has a new design with added functionality: You can now create subcategories, duplicate questions and add custom correct and incorrect answer texts
 - New Lesson Studio portlet added with a wysiwyg slide editor for lessons: Add text, images, videos, PDF files, embedded content, pptx files, questions or math functions to slides
 - Lesson Designer lessons can be exported to new Lesson Studio portlet
 - New dashboard landing page for students: Displays statistical information concerning your learning courses, certificates, learning paths and activities
 - Removed file size limit of 15mb for uploaded lesson packages
 - Padding lessons created in Lesson Studio on mobile device fixed
 - Needed fixes for reports done

## Version 2.2 Update 11.12.2014
 - Uploading pptx presentation as TinCan package
 - Uploading pptx presentation as list of questions in Lesson Designer
 - Moving questions between courses, displaying all questions from Liferay instance
 - Logo change will affect only after Save action
 - Scandic letters problem fix
 - Time limit for passing package
 - Time limit between package retakes
 - Check mark for package which user already started, displaying remain attemps
 - PDF viewer
 - Theme selector for tincan
 - Randomization questions in tincan packages
 - Foreground video
 - Printing Learning transcript
 - Fluid card layout
 - Tincan signed statements support
 - Removed instructional sentences (such as "choose correct answers" ) from questions
 - UI improvements

## Version 2.0 Update 19.6.2014
 - New UI with RWD
 - Gradebook for LRS
 - New reports
 - Updated curriculum functionality
 - Fullscreen mode for Lesson Viewer 

## Version 1.7 Update 28.2.2014
 - OAuth identity provider for TinCan LRS
 - TinCan statements viewer and reporting
 - Separate Package manager and Administering portlets 
 - Personal scope for packages
 - Impoved user searching for certificates and achievements
 - Oracle DB support
 - Improvements and fixes

## Version 1.6.6 Update 31.1.2014
**Liferay 6.2 support. Liferay 6.1 is not supported since this version**
 - Liferay 6.2 support
 - Achievements portlets

## Version 1.6.1 Update 10.1.2014
 - Tin Can API LRS - Learning Record Store implementation
 - Small improvements and fixes

## Version 1.5.6 Update: 14.11.2013
 - Support for Liferay 6.1.2 GA3
 - Support for Liferay 6.1.30 EE
 - Special characters issue in LIFERAY_HOME path
 - Question management - Numberic question issue
 - Search hook fix for Liferay 6.1.2 GA3
 - Relative URL conversion fix in TinyMCE
 - Fix for Liferay web content scopes
 - Fix for Gradebook instance-wide visibility in multi-instance installation
 - Curriculum - Fix for user visibility issue in multi-instance installation
 - Curriculum - Fix for course site link issue
 - Fix for database update issue with Service Builder
 - Fix for Openbadges.me integration
 - Name refactoring - Removing SCORM references

## Version 1.5.1 Update: 16.10.2013

Fixed problems:
 - JavaScript Minifier failed for SCORM Gradebook viewer
 - Quiz portlet shows only main instance LF Articles
 - Gradebook matrix view shows all quizes even from another courses
 - Reordering questions and categories is not working properly

## Version 1.5 Update: 9.10.2013
 - Tin Can API support
 - Integration connecting 3rd party Tin Can LRS with Basic Auth
 - Open Badges integration for issuing and earning badges
 - My Certificates -portlet
 - Badges functionality to Curriculum
 - Integration to Open Badges to show your earned Open Badges in My Certificates portlet
 - Badge Designer integration to Curriculum
 - Badge uploading
 - Curriculum improvements
 - Gradebook matrix view, to see all students for a course
 - Small improvements and bug fixes

## Version 1.4.5 Update: 13.08.2013
 - new Curriculum portlet for managing certifications, awards and learning paths
 - demo hook updated and now create private site and site pages

## Version 1.4 Update: 24.05.2013
 - Persistence reimplemented using Liferay Service Builder, so there is no database settings in Admin portlet.
 - Demo Hook updated and now it add additional demo content into separate site
 - Refactoring, Bugs fixing
 

## Version 1.3 Update: 18.03.2013
 - MySQL support
 - Demo hook
 - IsDefault property
 - Updater portlet
 - Bugs fixes

## Version 1.2.1 Update: 22.01.2013
 - Implemented Scope to all portlets
 - Added possibility to add manual comment and grade for essay and for whole course
 - Player continue playing course at the same location when user reload page
 - Static user roles for permissions: Student and Teacher
 

## Version 1.2. Update: 07.11.2012
 - Out of the box H2 database
 - Support of external resources (now you can add page by full URL, like http://www.example.com)
 - Support of Liferay's articles with article pickup dialog
 - Question preview from Quiz management
 - Packages now can be found and accessed from AssetPublisher and Search portlets from Liferay
 - L18N support based on Liferay's locale
 - Small fixes for UI
 - Fix for Liferay bundle with JBoss

## Version 1.1. Update: 07.09.2012
 - Support for SCORM 1.2
 - Small fixes for UI

## Version 1.0.1. Update: 29.08.2012
 - 'Redactor' replaced with TinyMCE
 - bugfixing

## Version 1.0 - Release. Update: 16.08.2012
 - SCORM 2004 4th Ed. support improved in part of Sequencing and RTE
 - Added quiz generation support
 - Added gradebook portlet
 - UI refactored
 - Source code refactoring and unit testing

## Version 0.3 - Question base portlet stabilized. Update: 06.02.2012
 - Added functionality to upload and add image and file attachements in Questionbank.
 - Added drag-n-dropfor question and category in TreeView
 - A lot of small improvements regarding to UI
 