import { Module } from '@nestjs/common';
import { ContratoProtocoloComunicacionService } from './contrato-protocolo-comunicacion.service.js';

@Module({
  providers: [ContratoProtocoloComunicacionService],
  exports: [ContratoProtocoloComunicacionService],
})
export class ContratoProtocoloComunicacionModule {}
