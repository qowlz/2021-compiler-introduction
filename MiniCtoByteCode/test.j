.class public Test
.super java/lang/Object

.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method

.method public static add(II)I
	.limit stack 32
	.limit locals 32

iload 0 
iload 1 
iadd 
istore 2 
iload 2 

ireturn
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 32
	.limit locals 32
ldc 33
istore 2

getstatic java/lang/System/out Ljava/io/PrintStream; 


ldc 1 
iload 2 
invokestatic Test/add(II)I
invokevirtual java/io/PrintStream/println(I)V
return
.end method


