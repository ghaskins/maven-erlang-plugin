File target = new File(basedir, "target/compile-0/ebin/plugin_compile.beam");
if (!target.isFile()) {
    throw new IllegalStateException("The compiled target " + target + " was missing.");
}