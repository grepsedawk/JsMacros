package xyz.wagyourtail.jsmacros.core.language.impl;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.Context.Builder;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class JavascriptLanguageDefinition extends BaseLanguage {
    private static final Builder build = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .allowHostClassLookup(s -> true)
        .allowAllAccess(true)
        .allowIO(true)
        .allowExperimentalOptions(true)
        .option("js.commonjs-require", "true");
    
    public JavascriptLanguageDefinition(String extension, Core runner) {
        super(extension, runner);
    }
    
    private Context buildContext(Path currentDir, Map<String, Object> globals) throws IOException {
        if (runner.config.options.extraJsOptions == null)
            runner.config.options.extraJsOptions = new LinkedHashMap<>();
        build.options(runner.config.options.extraJsOptions);
        build.option("js.commonjs-require-cwd", currentDir.toFile().getCanonicalPath());
        if (currentDir != null) build.currentWorkingDirectory(currentDir);
        final Context con = build.build();
        
        // Set Bindings
        final Value binds = con.getBindings("js");
        
        if (globals != null) globals.forEach(binds::putMember);
        
        retrieveLibs(con).forEach(binds::putMember);
        
        return con;
    }
    
    @Override
    public void exec(ScriptTrigger macro, File file, BaseEvent event) throws Exception {
        Map<String, Object> globals = new HashMap<>();
        
        globals.put("event", event);
        globals.put("file", file);
        
        final Context con = buildContext(file.getParentFile().toPath(), globals);
        con.eval(Source.newBuilder("js", file).build());
    }
    
    @Override
    public void exec(String script, Map<String, Object> globals, Path currentDir) throws Exception {
        final Context con = buildContext(currentDir, globals);
        con.eval("js", script);
    }
    
    @Override
    public BaseWrappedException<?> wrapException(Throwable ex) {
        if (ex instanceof PolyglotException) {
            Iterator<PolyglotException.StackFrame> frames = ((PolyglotException) ex).getPolyglotStackTrace().iterator();
            SourceSection pos = ((PolyglotException) ex).getSourceLocation();
            BaseWrappedException.SourceLocation loc = null;
            if (pos != null) {
                loc = new BaseWrappedException.GuestLocation(new File(pos.getSource().getPath()), pos.getCharIndex(), pos.getCharEndIndex(), pos.getStartLine(), pos.getStartColumn());
            }
            return new BaseWrappedException<>(ex, ex.getMessage(), loc, frames.hasNext() ? internalWrap(frames.next(), frames) : null);
        }
        return null;
    }
    
    private BaseWrappedException<?> internalWrap(PolyglotException.StackFrame current, Iterator<PolyglotException.StackFrame> frames) {
        if (current == null) return null;
        if (current.isGuestFrame()) {
            SourceSection pos = current.getSourceLocation();
            return new BaseWrappedException<>(current, " at " + current.getRootName(), new BaseWrappedException.GuestLocation(new File(pos.getSource().getPath()), pos.getCharIndex(), pos.getCharEndIndex(), pos.getStartLine(), pos.getStartColumn()), frames.hasNext() ? internalWrap(frames.next(), frames) : null);
        }
        return BaseWrappedException.wrapHostElement(current.toHostFrame(), frames.hasNext() ? internalWrap(frames.next(), frames) : null);
    }
}
