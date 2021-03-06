/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.reil.translators.x86;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyX86;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.x86.LahfTranslator;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class LahfTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX86(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final LahfTranslator translator = new LahfTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testAuxiliaryIntoCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x1200), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testAuxiliaryIntoSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFFC7FFL), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testCarryIntoCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x0300), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testCarryIntoSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFFD6FFL), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testParityIntoCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x0600), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testParityIntoSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFFD3FFL), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testSignIntoCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x8200), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testSignIntoSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFF57FFL), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testZeroIntoCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x4200), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testZeroIntoSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("lahf", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFF97FFL), interpreter.getVariableValue("eax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
