import { Module } from '@nestjs/common';
import { CompGestorColasMensajesService } from './comp-gestor-colas-mensajes.service.js';
import { GestorColasService } from './gestor-colas.service.js';

@Module({
  providers: [CompGestorColasMensajesService, GestorColasService],
  exports: [CompGestorColasMensajesService],
})
export class CompGestorColasMensajesModule {}
