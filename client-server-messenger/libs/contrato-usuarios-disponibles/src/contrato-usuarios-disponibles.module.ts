import { Module } from '@nestjs/common';
import { ContratoUsuariosDisponiblesService } from './contrato-usuarios-disponibles.service.js';

@Module({
  providers: [ContratoUsuariosDisponiblesService],
  exports: [ContratoUsuariosDisponiblesService],
})
export class ContratoUsuariosDisponiblesModule {}
