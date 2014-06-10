package org.shellty.compiler;

import java.util.List;

import org.shellty.compiler.semantic.Node;
import org.shellty.compiler.semantic.NodeData.NodeType;
import org.shellty.compiler.semantic.Tree;
import org.shellty.utils.Logger;

public class CodeGen {
    private String mResult = "";
    private int indent = 0;

    public int incIndent() {
        return ++indent;
    }

    public int decIndent() {
        return --indent;
    }

    private String indents() {
        String res = "";
        for (int i = 0; i < indent ; i++) {
            res += "\t";
        }
        return res;
    }

    public void insertLine(String line) {
        mResult += "\n" + indents() + line;
    }

    public void insertLine() {
        mResult += "\n";
    }

    public void insertSymbols(String symbols) {
        mResult += indents() + symbols;
    }

    public void insertVarDeclaration(Node varNode) {
        String varName = varNode.getData().getLexem();
        Logger.getInstance().log(varNode.getData());
        if (varNode.getData().isArrayVar()) {
            insertLine("local -A " + varName);
            return;    
        }
       
        switch (varNode.getData().getType()) {
        case COMPLEXVAR:
            break;
        case ENUMVAR:
        case STRING:
            insertLine(String.format("local %s", varName));
            if (!varNode.getData().getValue().isEmpty()) {
                insertSymbols("=" + varNode.getData().getValue());
            }
            return;
        case INTEGER:
            insertLine(String.format("local -i %s", varName));
            if (!varNode.getData().getValue().isEmpty()) {
                insertSymbols("=" + varNode.getData().getValue());
            }
            return;
        default:
            return;
        }

        Node structNode = varNode.getRightNode();
        if (structNode == null) {
            // TODO: generate exception
        }

        List<Node> fields = Tree.getFieldsStruction(structNode);
        String fieldsString = "";
        for (Node field : fields) {
            fieldsString += String.format("[%s]", field.getData().getLexem());
            if (!varNode.getData().getValue().isEmpty()) {
                fieldsString += "="+varNode.getData().getValue();
            } else {
                fieldsString += "=\"\"";
            }
            fieldsString += " ";
        }
        insertLine("declare -a " + varName + "=(" + fieldsString + ")");
    }

    public void insertParametrDeclaration(Node parametrNode, int number_parameter) {
        final String formatDeclare = "local %s %s=%s";
        if (parametrNode.getData().isArrayVar()) {
            insertLine(String.format(formatDeclare, "-a",
                        parametrNode.getData().getLexem(),
                        "(\"${!" + number_parameter + "}\")"));
            return;                          
        }

        switch (parametrNode.getData().getType()) {
        case COMPLEXVAR:
           insertLine("eval \"declare -A " + parametrNode.getData().getLexem() + 
                   "=\"${" + number_parameter + "#*=}");
            return;
        case INTEGER:
            insertLine(String.format(formatDeclare, "-i",
                        parametrNode.getData().getLexem(), 
                        "$" + number_parameter));
            return;
        case ENUMVAR:
        case STRING:
            insertLine(String.format(formatDeclare, "",
                        parametrNode.getData().getLexem(), 
                        "$" + number_parameter));
            return;
        default:
        }
    }

    public void insertStringLiteral(String str) {
        insertSymbols("\"" + str + "\"");
    }

    public String getResult() {
        return mResult;
    }

}

