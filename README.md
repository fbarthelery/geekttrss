Geekttrss
==========

Geekttrss is an Tiny Rss reader application with transparent offline mode.

You will need to install the web application [Tiny Tiny Rss](https://tt-rss.org/) and enable API access.
Then you will be able to access your Tiny Tiny Rss account from anywhere.

Geekttrss is an open source application and licensed under the GNU General Public License 3 and any later version.
This means that you can get Geekttrss's code and modify it to suit your needs, as long as you publish the changes
you make for everyone to benefit from as well.

Geekttrss is built and maintained by community volunteers.


Build variants
==============

The project builds in 2 flavors :

   * The Google flavor is distributed on the [Google Play store](https://play.google.com/store/apps/details?id=com.geekorum.ttrss).
     It uses Crashlytics and other Google Play services to retrieve crash reports.

   * The free flavor is not distributed but can be build from the sources. It doesn't contains any Google Play services.


Build instructions
==================

Just use Gradle to build

    ./gradlew build
