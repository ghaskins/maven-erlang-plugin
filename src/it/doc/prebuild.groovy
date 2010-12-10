File doc = new File(basedir, "target/site/edoc/index.html");
if (doc.isFile()) {
    throw new IllegalStateException("EDoc index page " + doc + " must not exist before test is run.");
}
