/**
 * Copyright 2015 Apigee Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.apigee.trireme.node12.modules;

import io.apigee.trireme.core.InternalNodeModule;
import io.apigee.trireme.core.NodeException;
import io.apigee.trireme.core.NodeRuntime;
import io.apigee.trireme.core.ScriptTask;
import io.apigee.trireme.core.Utils;
import io.apigee.trireme.core.internal.AbstractIdObject;
import io.apigee.trireme.core.internal.IdPropertyMap;
import io.apigee.trireme.core.internal.ScriptRunner;
import io.apigee.trireme.core.modules.Buffer;
import io.apigee.trireme.node12.internal.Compressor;
import io.apigee.trireme.node12.internal.Decompressor;
import io.apigee.trireme.node12.internal.ZlibWriter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import static io.apigee.trireme.core.ArgUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

public class ZlibWrap
    implements InternalNodeModule
{
    public static final int
        Z_PARTIAL_FLUSH = 1,
        Z_FINISH = 4,
        Z_BLOCK = 5,
        Z_TREES = 6,
        Z_OK = 0,
        Z_STREAM_END = 1,
        Z_NEED_DICT = 2,
        Z_ERRNO = -1,
        Z_STREAM_ERROR = -2,
        Z_DATA_ERROR = -3,
        Z_MEM_ERROR = -4,
        Z_BUF_ERROR = -5,
        Z_VERSION_ERROR = -6,
        Z_RLE = 3,
        Z_FIXED = 4;

    public static final String
        ZLIB_VERNUM = "1",
        ZLIB_VERSION = "1.0";

    @Override
    public String getModuleName() {
        return "zlib";
    }

    @Override
    public Scriptable registerExports(Context cx, Scriptable global, NodeRuntime runtime)
        throws InvocationTargetException, IllegalAccessException, InstantiationException
    {
        Scriptable exports = cx.newObject(global);
        Function zlib = new ZlibImpl().exportAsClass(global);
        exports.put(ZlibImpl.CLASS_NAME, exports, zlib);
        setConstants(exports);
        return exports;
    }

    private void setConstants(Scriptable exports)
    {
        setConstant("Z_NO_FLUSH", Deflater.NO_FLUSH, exports);
        setConstant("Z_PARTIAL_FLUSH", Z_PARTIAL_FLUSH, exports);
        setConstant("Z_SYNC_FLUSH", Deflater.SYNC_FLUSH, exports);
        setConstant("Z_FULL_FLUSH", Deflater.FULL_FLUSH, exports);
        setConstant("Z_FINISH", Z_FINISH, exports);
        setConstant("Z_BLOCK", Z_BLOCK, exports);
        setConstant("Z_TREES", Z_TREES, exports);
        setConstant("Z_OK", Z_OK, exports);
        setConstant("Z_STREAM_END", Z_STREAM_END, exports);
        setConstant("Z_NEED_DICT", Z_NEED_DICT, exports);
        setConstant("Z_ERRNO", Z_ERRNO, exports);
        setConstant("Z_STREAM_ERROR", Z_STREAM_ERROR, exports);
        setConstant("Z_DATA_ERROR", Z_DATA_ERROR, exports);
        setConstant("Z_MEM_ERROR", Z_MEM_ERROR, exports);
        setConstant("Z_BUF_ERROR", Z_BUF_ERROR, exports);
        setConstant("Z_VERSION_ERROR", Z_VERSION_ERROR, exports);
        setConstant("Z_NO_COMPRESSION", Deflater.NO_COMPRESSION, exports);
        setConstant("Z_BEST_SPEED", Deflater.BEST_SPEED, exports);
        setConstant("Z_BEST_COMPRESSION", Deflater.BEST_COMPRESSION, exports);
        setConstant("Z_DEFAULT_COMPRESSION", Deflater.DEFAULT_COMPRESSION, exports);
        setConstant("Z_FILTERED", Deflater.FILTERED, exports);
        setConstant("Z_HUFFMAN_ONLY", Deflater.HUFFMAN_ONLY, exports);
        setConstant("Z_RLE", Z_RLE, exports);
        setConstant("Z_FIXED", Z_FIXED, exports);
        setConstant("Z_DEFAULT_STRATEGY", Deflater.DEFAULT_STRATEGY, exports);
        setConstant("DEFLATE", ZlibWriter.DEFLATE, exports);
        setConstant("INFLATE", ZlibWriter.INFLATE, exports);
        setConstant("GZIP", ZlibWriter.GZIP, exports);
        setConstant("GUNZIP", ZlibWriter.GUNZIP, exports);
        setConstant("DEFLATERAW", ZlibWriter.DEFLATERAW, exports);
        setConstant("INFLATERAW", ZlibWriter.INFLATERAW, exports);
        setConstant("UNZIP", ZlibWriter.UNZIP, exports);
        setConstant("ZLIB_VERSION", ZLIB_VERSION, exports);
        setConstant("ZLIB_VERNUM", ZLIB_VERNUM, exports);
    }

    private void setConstant(String name, Object value, Scriptable exports)
    {
        ScriptableObject.defineProperty(exports, name, value, ScriptableObject.CONST);
    }

    public static class ZlibImpl
        extends AbstractIdObject<ZlibImpl>
    {
        public static final String CLASS_NAME = "Zlib";

        private static final IdPropertyMap props = new IdPropertyMap(CLASS_NAME);

        private static final int
            Id_write = 1,
            Id_writeSync = 2,
            Id_init = 3,
            Id_params = 4,
            Id_reset = 5,
            Id_close = 6,
            Id_onerror = 2;

        static {
            props.addMethod("write", Id_write, 7);
            props.addMethod("writeSync", Id_writeSync, 7);
            props.addMethod("init", Id_init, 5);
            props.addMethod("params", Id_params, 2);
            props.addMethod("reset", Id_reset, 0);
            props.addMethod("close", Id_close, 0);
            props.addProperty("onerror", Id_onerror, 0);
        }

        private final int mode;
        private ScriptRunner runtime;
        /** Callback: (message, errno) */
        private Function onError;
        private ZlibWriter writer;

        @Override
        protected ZlibImpl defaultConstructor(Context cx, Object[] args)
        {
            int mode = intArg(args, 0);
            return new ZlibImpl(cx, mode);
        }

        @Override
        protected ZlibImpl defaultConstructor()
        {
            throw new AssertionError();
        }

        public ZlibImpl()
        {
            super(props);
            this.mode = 0;
        }

        public ZlibImpl(Context cx, int mode)
        {
            super(props);
            this.mode = mode;
            this.runtime = (ScriptRunner)cx.getThreadLocal(ScriptRunner.RUNNER);
        }

        @Override
        public Object getInstanceIdValue(int id)
        {
            switch (id) {
            case Id_onerror:
                return onError;
            default:
                return super.getInstanceIdValue(id);
            }
        }

        @Override
        public void setInstanceIdValue(int id, Object value)
        {
            switch (id) {
            case Id_onerror:
                onError = (Function)value;
                break;
            default:
                super.setInstanceIdValue(id, value);
                break;
            }
        }

        @Override
        protected Object prototypeCall(int id, Context cx, Scriptable scope, Object[] args)
        {
            switch (id) {
            case Id_write:
                write(cx, true, args);
                break;
            case Id_writeSync:
                write(cx, false, args);
                break;
            case Id_init:
                init(cx, args);
                break;
            case Id_params:
                params(args);
                break;
            case Id_reset:
                reset();
                break;
            case Id_close:
                close();
                break;
            default:
                return super.prototypeCall(id, cx, scope, args);
            }
            return Undefined.instance;
        }

        private void init(Context cx, Object[] args)
        {
            // arg 0 is "windowBits". Java doesn't care.
            int level = intArg(args, 1);
            // arg 2 is "memLevel". Java doesn't care.
            int strategy = intArg(args, 3);
            Buffer.BufferImpl dictionary = objArg(args, 4, Buffer.BufferImpl.class, false);
            ByteBuffer dictBuf =
                (dictionary == null ? null : dictionary.getBuffer());

            try {
                switch (mode) {
                case ZlibWriter.DEFLATE:
                case ZlibWriter.DEFLATERAW:
                case ZlibWriter.GZIP:
                    writer = new Compressor(mode, level, strategy, dictBuf);
                    break;
                case ZlibWriter.INFLATE:
                case ZlibWriter.INFLATERAW:
                case ZlibWriter.GUNZIP:
                case ZlibWriter.UNZIP:
                    writer = new Decompressor(mode, dictBuf);
                    break;
                default:
                    throw Utils.makeError(cx, this, "Invalid compression mode " + mode);
                }
            } catch (NodeException ne) {
                throw Utils.makeError(cx, this, "Error initializing compression: " + ne);
            }
        }

        private void params(Object[] args)
        {
            int level = intArg(args, 0);
            int strategy = intArg(args, 1);

            writer.setParams(level, strategy);
        }

        private void reset()
        {
            writer.reset();
        }

        private void close()
        {
            writer.close();
        }

        private Scriptable write(Context cx, boolean async, Object[] args)
        {
            final int flushFlag = intArg(args, 0);
            ensureArg(args, 1);
            int inOff = intArg(args, 2);
            int inLen = intArg(args, 3);
            Buffer.BufferImpl out = objArg(args, 4, Buffer.BufferImpl.class, true);
            int outOff = intArg(args, 5);
            int outLen = intArg(args, 6);

            // "in" could be null
            Buffer.BufferImpl in;
            if ((args[1] == null) || Undefined.instance.equals(args[1])) {
                in = null;
            } else {
                in = (Buffer.BufferImpl)args[1];
            }

            final ByteBuffer inBuf = (in == null ? null :  in.getBuffer());
            if (inBuf != null) {
                inBuf.position(inBuf.position() + inOff);
                inBuf.limit(inBuf.position() + inLen);
            }

            final ByteBuffer outBuf = out.getBuffer();
            outBuf.position(outBuf.position() + outOff);
            outBuf.limit(outBuf.position() + outLen);

            if (async) {
                // In async mode, "write" expects an object that it can stick stuff on
                final Scriptable writeResponse = cx.newObject(this);

                // They said async, so we might as well run this in the thread pool and use more cores
                runtime.pin();
                runtime.getAsyncPool().submit(new Runnable() {
                    @Override
                    public void run()
                    {
                        writeAsync(flushFlag, writeResponse, inBuf, outBuf);
                    }
                });
                return writeResponse;

            } else {
                try {
                    writer.write(flushFlag, inBuf, outBuf);
                } catch (DataFormatException dfe) {
                    if (onError != null) {
                        // Sadly Java does not tell us as much about the error as Node would like
                        onError.call(cx, onError, this,
                                     new Object[] { dfe.toString(), Z_DATA_ERROR });
                    }
                }

                // In sync mode, "writeSync" expects an array with the remaining data in it
                Scriptable result = cx.newArray(this, 2);
                result.put(0, result, (inBuf == null ? 0 : inBuf.remaining()));
                result.put(1, result, outBuf.remaining());
                return result;
            }
        }

        private void writeAsync(int flushFlag, final Scriptable response,
                                final ByteBuffer inBuf, final ByteBuffer outBuf)
        {
            try {
                try {
                    writer.write(flushFlag, inBuf, outBuf);
                    runtime.enqueueTask(new ScriptTask() {
                        @Override
                        public void execute(Context cx, Scriptable scope)
                        {
                            completeWrite(cx, response, inBuf, outBuf, null);
                        }
                    });
                } catch (final DataFormatException dfe) {
                    runtime.enqueueTask(new ScriptTask() {
                        @Override
                        public void execute(Context cx, Scriptable scope)
                        {
                            completeWrite(cx, response, inBuf, outBuf, dfe);
                        }
                    });
                }
            } finally {
                runtime.unPin();
            }
        }

        private void completeWrite(Context cx, Scriptable response,
                                   ByteBuffer inBuf, ByteBuffer outBuf,
                                   DataFormatException err)
        {
            if ((err != null) && (onError != null)) {
                onError.call(cx, onError, this,
                             new Object[] { err.toString(), Z_DATA_ERROR });
            }
            // Once we get here, a callback should be set on the "response"
            Function cb = (Function)response.get("callback", response);
            if (cb != null) {
                cb.call(cx, this, response, new Object[] {
                    (inBuf == null ? 0 : inBuf.remaining()),
                    outBuf.remaining()
                });
            }
        }
    }
}