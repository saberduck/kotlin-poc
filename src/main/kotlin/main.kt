import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.children
import java.nio.file.Files
import java.nio.file.Paths

// COMMENT
fun main(args: Array<String>) {
    val file = System.getProperty("file") ?: throw IllegalArgumentException("Set file with -Dfile=source.kt")
    val content = String(Files.readAllBytes(Paths.get(file)))
    val ktFile = compile(content)
    ktFile.node.children().forEach { printAstNode(it) }
//    ktFile.children.forEach { printPsiElement(it) }
}

fun printAstNode(astNode: ASTNode, indent: Int = 0) {
    print("".padStart(indent))
    println(astNode.elementType.toString() + "[" + astNode.textRange + "]" )
    astNode.children().forEach { printAstNode(it, indent + 2) }
}

fun printPsiElement(el: PsiElement, indent: Int = 0) {
    print("".padStart(indent))
    println(el)
    el.children.forEach { printPsiElement(it, indent + 2) }
}

fun compile(content: String): KtFile {
    val psiFileFactory: PsiFileFactory = PsiFileFactory.getInstance(createKotlinCoreEnvironment())
    return psiFileFactory.createFileFromText(KotlinLanguage.INSTANCE, content) as KtFile
}

private fun createKotlinCoreEnvironment(): Project {
    System.setProperty("idea.io.use.fallback", "true")
    val configuration = CompilerConfiguration()
    configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false))
    return KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(),
            configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES).project
}
