import { Module } from '@nestjs/common';
import { CompUsuariosService } from './comp-usuarios.service.js';
import { UsuariosService } from './usuarios.service.js';

@Module({
  providers: [CompUsuariosService, UsuariosService],
  exports: [CompUsuariosService],
})
export class CompUsuariosModule {}
