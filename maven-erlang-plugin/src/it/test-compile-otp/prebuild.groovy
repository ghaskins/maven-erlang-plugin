File target = new File(basedir, "target/test-compile-otp-0-test/ebin/some_test.beam");
if (target.isFile()) {
    throw new IllegalStateException("The compiled test target " + target + " must not exist before test is run.");
}