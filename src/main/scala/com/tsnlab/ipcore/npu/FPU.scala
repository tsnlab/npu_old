package com.tsnlab.ipcore.npu

import chisel3._
import chisel3.util.{switch, is, MuxLookup}
import fudian.{FADD,FDIV,FMUL}

class FPU(exponent: Int, mantissa: Int) extends Module {
  val data = IO(new Bundle{
    val a = Input(UInt((exponent + mantissa + 1).W))
    val b = Input(UInt((exponent + mantissa + 1).W))
    val y = Output(UInt((exponent + mantissa + 1).W))
  })
  val control = IO(new Bundle{
    val op = Input(UInt(2.W))
  })

  val fadd = Module(new FADD(exponent, mantissa))
  val fdiv = Module(new FDIV(exponent, mantissa))
  val fmul = Module(new FMUL(exponent, mantissa))

  fadd.io.a  := data.a
  fdiv.io.a  := data.a
  fmul.io.a  := data.a
  fadd.io.b  := data.b
  fdiv.io.b  := data.b
  fmul.io.b  := data.b
  fadd.io.rm := 0.U
  fdiv.io.rm := 0.U
  fmul.io.rm := 0.U

  // TODO: Wire it properly
  fdiv.io.specialIO.in_valid := 1.B
  fdiv.io.specialIO.kill := 0.B
  fdiv.io.specialIO.out_ready := 1.B
  fdiv.io.specialIO.isSqrt := 0.B

  data.y := MuxLookup(control.op, "hFFFF_FFFF".U, Array(
    0.U -> fadd.io.result,
    1.U -> "hFFFF_FFFF".U,
    2.U -> fdiv.io.result,
    3.U -> fmul.io.result,
  ))
}
