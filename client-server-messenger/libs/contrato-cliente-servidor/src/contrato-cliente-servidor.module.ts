import { Module } from '@nestjs/common';
import { ContratoClienteServidorService } from './contrato-cliente-servidor.service.js';

@Module({
  providers: [ContratoClienteServidorService],
  exports: [ContratoClienteServidorService],
})
export class ContratoClienteServidorModule {}
