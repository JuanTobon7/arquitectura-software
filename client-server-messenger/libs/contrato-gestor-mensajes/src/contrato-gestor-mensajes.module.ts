import { Module } from '@nestjs/common';
import { ContratoGestorMensajesService } from './contrato-gestor-mensajes.service.js';

@Module({
  providers: [ContratoGestorMensajesService],
  exports: [ContratoGestorMensajesService],
})
export class ContratoGestorMensajesModule {}
