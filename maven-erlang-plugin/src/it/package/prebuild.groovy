File target = new File(basedir, "target/package-0.tar.gz");
if (target.isFile()) {
    throw new IllegalStateException("Target package file " + target + " must not exists before tests are run.");
}
