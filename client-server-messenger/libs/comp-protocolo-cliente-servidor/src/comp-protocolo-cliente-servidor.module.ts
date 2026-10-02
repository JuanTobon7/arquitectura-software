import { Module } from '@nestjs/common';
import { CompProtocoloClienteServidorService } from './comp-protocolo-cliente-servidor.service.js';
import { ServidorTcpService } from './servidor-tcp.service.js';

@Module({
  providers: [CompProtocoloClienteServidorService, ServidorTcpService],
  exports: [CompProtocoloClienteServidorService],
})
export class CompProtocoloClienteServidorModule {}
