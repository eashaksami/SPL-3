package com.example.bug_localizer.test;

import com.example.bug_localizer.utils.FileReader;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AstParser {

    List<String> allClassNames = new ArrayList<>();
    List<String> allMethodNames = new ArrayList<>();
    List<String> allFieldSignatures = new ArrayList<>();

    public CompilationUnit getCompilationUnit(String filePath) throws IOException {
        FileReader fileReader = new FileReader();
        String content = fileReader.readFile(filePath);
        ASTParser parser = ASTParser.newParser(AST.JLS18);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
    }

    public List<String> getAllClassNames(CompilationUnit compilationUnit) {
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(TypeDeclaration type) {
                allClassNames.add(type.getName().toString());
                return super.visit(type);
            }
        });
        return allClassNames;
    }

    public List<String> getAllMethodNames(CompilationUnit compilationUnit) {
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(MethodDeclaration methodDecl) {
//                System.out.println("parameter list: "+methodDecl.parameters());
                allMethodNames.add(methodDecl.getName().getFullyQualifiedName());
                return super.visit(methodDecl);
            }
        });
        return allMethodNames;
    }

    public List<String> getAllFieldSignatures(CompilationUnit compilationUnit) {
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(FieldDeclaration node) {
                allFieldSignatures.add(node.getType().toString());
                List<VariableDeclarationFragment> variables = node.fragments();
                variables.forEach(v -> {
//                    System.out.println(v.getName().getIdentifier());
                    allFieldSignatures.add(v.getName().getIdentifier());
                });
                return super.visit(node);
            }
        });
        return allFieldSignatures;
    }

    public static void main(String[] args) throws IOException {
        AstParser astParser = new AstParser();

        String filePath = "/home/sami/Desktop/1538.java";
        CompilationUnit cu = astParser.getCompilationUnit(filePath);

        System.out.println(astParser.getAllClassNames(cu));

        System.out.println(astParser.getAllMethodNames(cu));

        System.out.println(astParser.getAllFieldSignatures(cu));

//        System.out.println(astParserTest.getAllFieldNames1(cu));
    }
}
