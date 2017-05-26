/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/
package org.snt.inmemantlr.graal;

public class GraalState {

    public enum Kind {
        ACCEPT,
        NORMAL
    }

    protected Kind kind;
    protected int id;

    public GraalState(Kind kind, int id){
        this.kind = kind;
        this.id = id;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public GraalState(int id) {
        this(Kind.NORMAL,id);
    }

    public boolean isAccept() {
        return kind == Kind.ACCEPT;
    }

    public boolean isNormal() {
        return kind == Kind.NORMAL;
    }

    public int getId() {
        return id;
    }

    public String toDot() {
        return "s" + id;
    }

    public GraalState clone() {
        return new GraalState(this.kind,this.id);
    }

    @Override
    public boolean equals(Object other) {

        if(!(other instanceof GraalState))
            return false;

        GraalState s = (GraalState)other;

        return this.id == s.id;
    }


}
