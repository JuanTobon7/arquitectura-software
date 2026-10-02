import { Module } from '@nestjs/common';
import { CompProtocoloComunicacionService } from './comp-protocolo-comunicacion.service.js';
import { ProtocoloLongitudJsonService } from './protocolo-longitud-json.service.js';

@Module({
  providers: [CompProtocoloComunicacionService, ProtocoloLongitudJsonService],
  exports: [CompProtocoloComunicacionService],
})
export class CompProtocoloComunicacionModule {}
