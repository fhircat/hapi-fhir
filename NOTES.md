# notes - burn before PR

## tried to s/r4/r5/g

```
org.hl7.fhir.common.hapi.validation.support.DefaultProfileValidationSupportNpmStrategy
result = {StackTraceElement[69]@2170} 
 0 = {StackTraceElement@2188} "java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)"
 1 = {StackTraceElement@2189} "java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)"
 2 = {StackTraceElement@2190} "java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)"
 3 = {StackTraceElement@2191} "java.base/java.lang.Class.forName0(Native Method)"
 4 = {StackTraceElement@2192} "java.base/java.lang.Class.forName(Class.java:375)"
 5 = {StackTraceElement@2193} "ca.uhn.fhir.util.ReflectionUtil.newInstance(ReflectionUtil.java:262)"
 6 = {StackTraceElement@2194} "ca.uhn.fhir.context.support.DefaultProfileValidationSupport.<init>(DefaultProfileValidationSupport.java:79)"
 7 = {StackTraceElement@2195} "ca.uhn.fhir.context.FhirContext.getValidationSupport(FhirContext.java:681)"
 8 = {StackTraceElement@2196} "org.uu3.sparql.SparqlTest.beforeClass(SparqlTest.java:70)"
 9 = {StackTraceElement@2197} "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"
 10 = {StackTraceElement@2198} "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)"
 11 = {StackTraceElement@2199} "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)"
 12 = {StackTraceElement@2200} "java.base/java.lang.reflect.Method.invoke(Method.java:568)"
 13 = {StackTraceElement@2201} "org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)"
 14 = {StackTraceElement@2202} "org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)"
 15 = {StackTraceElement@2203} "org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)"
 16 = {StackTraceElement@2204} "org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)"
 17 = {StackTraceElement@2205} "org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)"
 18 = {StackTraceElement@2206} "org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeAllMethod(TimeoutExtension.java:70)"
 19 = {StackTraceElement@2207} "org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)"
 20 = {StackTraceElement@2208} "org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)"
 21 = {StackTraceElement@2209} "org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)"
 22 = {StackTraceElement@2210} "org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)"
 23 = {StackTraceElement@2211} "org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)"
 24 = {StackTraceElement@2212} "org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)"
 25 = {StackTraceElement@2213} "org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)"
 26 = {StackTraceElement@2214} "org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)"
 27 = {StackTraceElement@2215} "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeBeforeAllMethods$13(ClassBasedTestDescriptor.java:411)"
 28 = {StackTraceElement@2216} "org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)"
 29 = {StackTraceElement@2217} "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeBeforeAllMethods(ClassBasedTestDescriptor.java:409)"
 30 = {StackTraceElement@2218} "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.before(ClassBasedTestDescriptor.java:215)"
 31 = {StackTraceElement@2219} "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.before(ClassBasedTestDescriptor.java:84)"
 32 = {StackTraceElement@2220} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:148)"
 33 = {StackTraceElement@2221} "org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)"
 34 = {StackTraceElement@2222} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)"
 35 = {StackTraceElement@2223} "org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)"
 36 = {StackTraceElement@2224} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)"
 37 = {StackTraceElement@2225} "org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)"
 38 = {StackTraceElement@2226} "org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)"
 39 = {StackTraceElement@2227} "org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)"
 40 = {StackTraceElement@2228} "java.base/java.util.ArrayList.forEach(ArrayList.java:1511)"
 41 = {StackTraceElement@2229} "org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)"
 42 = {StackTraceElement@2230} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)"
 43 = {StackTraceElement@2231} "org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)"
 44 = {StackTraceElement@2232} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)"
 45 = {StackTraceElement@2233} "org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)"
 46 = {StackTraceElement@2234} "org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)"
 47 = {StackTraceElement@2235} "org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)"
 48 = {StackTraceElement@2236} "org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)"
 49 = {StackTraceElement@2237} "org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)"
 50 = {StackTraceElement@2238} "org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)"
 51 = {StackTraceElement@2239} "org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)"
 52 = {StackTraceElement@2240} "org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)"
 53 = {StackTraceElement@2241} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:147)"
 54 = {StackTraceElement@2242} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:127)"
 55 = {StackTraceElement@2243} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:90)"
 56 = {StackTraceElement@2244} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:55)"
 57 = {StackTraceElement@2245} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:102)"
 58 = {StackTraceElement@2246} "org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:54)"
 59 = {StackTraceElement@2247} "org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:114)"
 60 = {StackTraceElement@2248} "org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:86)"
 61 = {StackTraceElement@2249} "org.junit.platform.launcher.core.DefaultLauncherSession$DelegatingLauncher.execute(DefaultLauncherSession.java:86)"
 62 = {StackTraceElement@2250} "org.junit.platform.launcher.core.SessionPerRequestLauncher.execute(SessionPerRequestLauncher.java:53)"
 63 = {StackTraceElement@2251} "com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:57)"
 64 = {StackTraceElement@2252} "com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)"
 65 = {StackTraceElement@2253} "com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)"
 66 = {StackTraceElement@2254} "com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)"
 67 = {StackTraceElement@2255} "com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:232)"
 68 = {StackTraceElement@2256} "com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:55)"
 ```

## SPARQL approach

**SPARQL Query**
```
{
?o a fhir:Observation ;
   fhir:subject [
     fhir:name [ rdf:first*/rdf:rest ?name ]
   ] ;
   fhir:code [ ... sct:1234 ]
   fhir:value [ ... ?value ]
}
```
**as triple patterns**
```
?o a fhir:Observation .
?o fhir:subject ?s .
?s fhir:name ?nl .
?nl rdf:first*/rdf:rest ?name .
?o fhir:code ?o1 .
?o1 fhir:v sct:1234 .
?o fhir:value ?o2 .
?o2 fhir:v ?value .
```

**map query patterns to FHIRPath primitives**
```
static QueryPatternToFilter = [
  { ?x fhir:code ?y . ?y rdf:first*/rdf:rest ... ?code } -> code(?code)
  ...
]
```
**find (a) root node in query**
```
Node rootResource = find(?x a fhir:Observation | fhir:Patient | ... )[0]
BaseRuntineElementDefinition root = ???("Observation")
foo(root, rootResource)
```
**iteratively walk node, point in ElementDefinition (and candidate points in patterns-to-FHIRPath?)**
```
foo(elementDef, subjectNode, graph, possibleFilters) {
  for each child in elementDef.getChildren() {
    predicate = constructPredicateName(...)
    for each (o in find(subjectNode, predicate, _)) {
      ...
      foo()
```

## misc reusable code

``` java
	public Bundle makeBundle(Resource... theResources) {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setTotal(theResources != null ? theResources.length : 0);
		if (theResources != null) {
			for (Resource l : theResources) {
				bundle.addEntry().setResource(l).setFullUrl("/" + l.fhirType() + "/" + l.getId());
			}
		}
		return bundle;
	}
```
