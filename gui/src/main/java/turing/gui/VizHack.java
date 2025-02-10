package turing.gui;

import guru.nidi.graphviz.engine.AbstractJavascriptEngine;
import guru.nidi.graphviz.engine.AbstractJsGraphvizEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import guru.nidi.graphviz.engine.JavascriptEngine;
import guru.nidi.graphviz.engine.ResultHandler;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static guru.nidi.graphviz.engine.GraphvizLoader.loadAsString;

public class VizHack extends AbstractJsGraphvizEngine {
    private static final ScriptEngineFactory NASHORN_FACTORY = new NashornScriptEngineFactory();
    private static final String PROMISE_JS_CODE = loadAsString("net/arnx/nashorn/lib/promise.js");

    private static Supplier<JavascriptEngine> engineSupplier() {
        var nashorn = NASHORN_FACTORY.getScriptEngine();
        try {
            nashorn.eval(PROMISE_JS_CODE);
        } catch (ScriptException e) {
            throw new GraphvizException("Nashorn error", e);
        }
        var resultHandler = new ResultHandler();
        nashorn.put("result", (Consumer<String>) resultHandler::setResult);
        nashorn.put("error", (Consumer<String>) resultHandler::setError);
        nashorn.put("log", (Consumer<String>) resultHandler::log);

        return () -> new AbstractJavascriptEngine() {
            @SuppressWarnings("NullableProblems")
            @Override
            protected String execute(String js) {
                try {
                    nashorn.eval(js);
                } catch (ScriptException e) {
                    throw new GraphvizException("Nashorn error", e);
                }
                return resultHandler.waitFor();
            }
        };
    }

    public VizHack() {
        super(false, engineSupplier());
    }
}
