package com.example.bug_localizer.test;

import com.example.bug_localizer.FileReader;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AstParserTest {

    List<String> allClassNames = new ArrayList<>();
    List<String> allMethodNames = new ArrayList<>();
    public CompilationUnit getCompilationUnit() throws IOException {
        FileReader fileReader = new FileReader();
        String content = fileReader.readFileFromBugReport("/home/sami/Desktop/1.java");
        ASTParser parser = ASTParser.newParser(AST.JLS18);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        return compilationUnit;
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
                allMethodNames.add(methodDecl.getName().getFullyQualifiedName());
                return super.visit(methodDecl);
            }
        });
        return allMethodNames;
    }

    public List<String> getAllFieldNames(CompilationUnit compilationUnit) {
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(SingleVariableDeclaration node) {
                allMethodNames.add(node.getName().getIdentifier());
                return super.visit(node);
            }
        });
        return allMethodNames;
    }

    public static void main(String[] args) throws IOException {
        AstParserTest astParserTest = new AstParserTest();

        CompilationUnit cu = astParserTest.getCompilationUnit();

        System.out.println(astParserTest.getAllClassNames(cu));

        System.out.println(astParserTest.getAllMethodNames(cu));

        System.out.println(astParserTest.getAllFieldNames(cu));
    }
}
