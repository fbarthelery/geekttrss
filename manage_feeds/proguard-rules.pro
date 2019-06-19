# Keep dynamically added DataBinderMapper from this feature module
# it should be added automatically but I guess this is a bug.
# TODO report or look for it
-keep class com.geekorum.ttrss.manage_feeds.DataBinderMapperImpl { *; }
